require 'json'
require 'open3'
require 'tmpdir'
require 'yaml'

class WorkflowGateTests
  ROOT = File.expand_path('../..', __dir__)
  REQUIRED_VALIDATIONS = %w[
    test mutation fuzz commitlint scorecard quality duplication
    security-audit codeql verify-docs sonar
  ].freeze

  def initialize
    @workflows = Dir[File.join(ROOT, '.github/workflows/*.yml')].to_h do |path|
      [File.basename(path), YAML.load_file(path)]
    end
    @jobs = @workflows.fetch('ci.yml').fetch('jobs')
  end

  def test_deployment_has_no_independent_trigger
    workflow = @workflows.fetch('deploy-to-clever-cloud.yml')

    configured_events = events(workflow).keys

    expect(configured_events == ['workflow_call'], 'Deployment must only be reusable')
    expect(@jobs.fetch('deploy').fetch('needs') == 'validate-main', 'Deployment must depend on validation')
  end

  def test_main_gate_covers_all_required_validations
    gate = @jobs.fetch('validate-main')

    dependencies = gate.fetch('needs')

    expect((REQUIRED_VALIDATIONS - dependencies).empty?, 'A required validation is missing from the gate')
    expect(gate.fetch('if').include?('always()'), 'The gate must report failed or skipped dependencies')
  end

  def test_reused_workflows_do_not_duplicate_push_or_pull_request_runs
    workflows = %w[security-audit.yml codeql.yml verify-docs.yml]

    configured_events = workflows.map { |name| events(@workflows.fetch(name)).keys }

    configured_events.each do |event_names|
      expect(event_names.include?('workflow_call'), 'Validation must be reusable')
      expect((event_names & %w[push pull_request pull_request_target]).empty?, 'Duplicate validation trigger')
    end
    %w[security-audit.yml codeql.yml].each do |name|
      expect(events(@workflows.fetch(name)).key?('schedule'), 'Independent security schedule was removed')
    end
  end

  def test_pull_request_security_has_no_repository_secrets_or_build
    caller = @jobs.fetch('dependency-review')
    review = @workflows.fetch('security-audit.yml').fetch('jobs').fetch('dependency-review')

    steps = review.fetch('steps')

    expect(!caller.key?('secrets'), 'PR security must not receive repository secrets')
    expect(caller.fetch('permissions') == { 'contents' => 'read' }, 'PR security must be read-only')
    expect(steps.none? { |step| step.key?('run') }, 'PR security must not execute repository scripts')
    expect(!review.to_json.include?('secrets.'), 'PR security references repository secrets')
  end

  def test_validation_gate_rejects_every_unsuccessful_dependency
    dependencies = @jobs.fetch('validate-main').fetch('needs')
    script = @jobs.fetch('validate-main').fetch('steps').first.fetch('run')
    successful = dependencies.to_h { |name| [name, { 'result' => 'success' }] }

    success = run_script(script, { 'VALIDATION_RESULTS' => successful.to_json })

    expect(success.success?, 'Successful validations must pass')

    dependencies.product(%w[failure cancelled skipped]).each do |name, conclusion|
      results = successful.merge(name => { 'result' => conclusion })

      result = run_script(script, { 'VALIDATION_RESULTS' => results.to_json })

      expect(!result.success?, "Gate accepted #{name}=#{conclusion}")
    end
  end

  def test_sonar_requires_success_for_the_exact_commit_and_app
    successful = sonar_check
    cases = {
      'success' => [[successful], true],
      'failure' => [[successful.merge('conclusion' => 'failure')], false],
      'neutral' => [[successful.merge('conclusion' => 'neutral')], false],
      'wrong SHA' => [[successful.merge('head_sha' => 'another-sha')], false],
      'wrong app ID' => [[successful.merge('app' => { 'id' => 999, 'slug' => 'sonarqubecloud' })], false],
      'wrong app slug' => [[successful.merge('app' => { 'id' => 12526, 'slug' => 'other-app' })], false],
      'missing' => [[], false],
      'pending' => [[successful.merge('status' => 'in_progress', 'conclusion' => nil)], false],
      'latest failed' => [[successful, successful.merge('id' => 2, 'conclusion' => 'failure')], false]
    }
    script = @jobs.fetch('sonar').fetch('steps').first.fetch('run')

    cases.each do |name, (checks, expected)|
      environment = sonar_environment(checks)

      result = run_script(script, environment, sonar_commands)

      expect(result.success? == expected, "Unexpected Sonar result for #{name}")
    end
  end

  def test_sonar_api_errors_fail_closed
    script = @jobs.fetch('sonar').fetch('steps').first.fetch('run')
    environment = sonar_environment([sonar_check]).merge('API_EXIT' => '1')

    result = run_script(script, environment, sonar_commands)

    expect(!result.success?, 'Sonar API errors must fail validation')
  end

  def test_sonar_wait_has_a_job_timeout
    job = @jobs.fetch('sonar')

    timeout = job.fetch('timeout-minutes', 0)

    expect(timeout.positive? && timeout <= 30, 'Sonar must have a bounded job timeout')
  end

  def test_deploy_rejects_unvalidated_or_stale_revisions
    deploy = @workflows.fetch('deploy-to-clever-cloud.yml').fetch('jobs').fetch('deploy')
    script = deploy.fetch('steps').find { |step| step['name'] == 'Deploy to Clever Cloud' }.fetch('run')
    environment = {
      'VALIDATED_REVISION' => 'validated-sha', 'GITHUB_SHA' => 'validated-sha',
      'CHECKOUT_SHA' => 'validated-sha', 'MAIN_SHA' => 'validated-sha',
      'CLEVER_TOKEN' => 'test', 'CLEVER_SECRET' => 'test',
      'APPLICATION_ID' => 'test', 'ORGA_ID' => 'test'
    }
    commands = {
      'git' => "case \"$1\" in\nrev-parse) echo \"$CHECKOUT_SHA\";;\nls-remote) printf '%s\\trefs/heads/main\\n' \"$MAIN_SHA\";;\ncheckout) exit 0;;\n*) exit 99;;\nesac\n",
      'clever' => "exit 0\n"
    }

    success = run_script(script, environment, commands)

    expect(success.success?, 'The validated current main commit must deploy')

    %w[GITHUB_SHA CHECKOUT_SHA MAIN_SHA].each do |key|
      mismatched = environment.merge(key => 'another-sha')

      result = run_script(script, mismatched, commands)

      expect(!result.success?, "Deployment accepted mismatched #{key}")
    end
  end

  private

  def events(workflow)
    workflow.fetch('on') { workflow.fetch(true) }
  end

  def expect(condition, message)
    raise message unless condition
  end

  def sonar_check
    {
      'head_sha' => 'validated-sha', 'name' => 'SonarCloud Code Analysis',
      'app' => { 'id' => 12526, 'slug' => 'sonarqubecloud' },
      'id' => 1, 'status' => 'completed', 'conclusion' => 'success'
    }
  end

  def sonar_environment(checks)
    {
      'CHECK_RESPONSE' => [{ 'check_runs' => checks }].to_json,
      'REVISION' => 'validated-sha', 'REPOSITORY' => 'owner/repository', 'API_EXIT' => '0'
    }
  end

  def sonar_commands
    {
      'gh' => "printf '%s\\n' \"$CHECK_RESPONSE\"\nexit \"$API_EXIT\"\n",
      'sleep' => "exit 0\n"
    }
  end

  def run_script(script, environment, commands = {})
    Dir.mktmpdir('workflow-gates-') do |directory|
      commands.each do |name, body|
        path = File.join(directory, name)
        File.write(path, "#!/bin/sh\n#{body}")
        File.chmod(0o755, path)
      end
      process_environment = environment.merge('PATH' => "#{directory}:#{ENV.fetch('PATH')}")

      _stdout, _stderr, status = Open3.capture3(process_environment, 'bash', '-c', script)

      status
    end
  end
end

tests = WorkflowGateTests.new
test_names = WorkflowGateTests.public_instance_methods(false).grep(/^test_/).sort
failures = test_names.map do |name|
  begin
    tests.public_send(name)
    puts "PASS #{name}"
    nil
  rescue StandardError => error
    warn "FAIL #{name}: #{error.message}"
    name
  end
end.compact
puts "#{test_names.length} workflow gate tests, #{failures.length} failures"
exit(failures.empty? ? 0 : 1)
