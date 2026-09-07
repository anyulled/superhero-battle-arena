#!/bin/sh
set -eu

repository_root=$(CDPATH='' cd -- "$(dirname -- "$0")/../.." && pwd)
hook_path="$repository_root/.githooks/pre-commit"
# Hook subprocesses inherit Git paths that must never target the caller's repository.
for repository_variable in $(git rev-parse --local-env-vars); do
  unset "$repository_variable"
done
test_directory=$(mktemp -d "${TMPDIR:-/tmp}/arena-hook-tests.XXXXXX")
trap 'rm -rf "$test_directory"' EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
export GIT_CONFIG_NOSYSTEM=1
export GIT_CONFIG_GLOBAL=/dev/null

fail() {
  echo "FAIL: $*" >&2
  [ ! -f temporary/output ] || cat temporary/output >&2
  exit 1
}

arrange_repository() {
  fixture_directory="$test_directory/$1"
  mkdir -p "$fixture_directory" "$fixture_directory/temporary"
  cd "$fixture_directory"
  git -c init.defaultBranch=test init --quiet
  git config user.name 'Hook Test'
  git config user.email 'hook-test@example.invalid'
  git config core.hooksPath "$fixture_directory/hooks"
  mkdir hooks
  cp "$hook_path" hooks/pre-commit
  chmod +x hooks/pre-commit
  printf 'temporary/\nhooks/\nignored-but-tracked.txt\n' > .gitignore
  printf 'original\n' > source.txt
  printf 'tracked\n' > ignored-but-tracked.txt
  cat > mvnw <<'MAVEN'
#!/bin/sh
set -eu
printf '%s\n' "$1" >> "$CALL_LOG"
[ "$(cat source.txt)" = staged ] || exit 91
[ ! -f private.txt ] || exit 92
[ "$(git show :ignored-but-tracked.txt)" = tracked ] || exit 94
[ "$(git rev-parse --show-toplevel)" = "$PWD" ] || exit 93
if [ "${FORMAT_COMMAND:-}" = "$1" ]; then
  printf 'formatted\n' > source.txt
fi
if [ "${FAIL_COMMAND:-}" = "$1" ]; then
  exit 42
fi
MAVEN
  chmod +x mvnw
  git add .gitignore source.txt mvnw
  git add --force ignored-but-tracked.txt
  git -c core.hooksPath=/dev/null commit --quiet -m baseline
  printf 'staged\n' > source.txt
  git add source.txt
  export CALL_LOG="$fixture_directory/temporary/calls"
  export TMPDIR="$fixture_directory/temporary"
  unset FAIL_COMMAND FORMAT_COMMAND
  original_index=$(git write-tree)
}

act_run_hook() {
  hook_status=0
  GIT_DIR="$fixture_directory/.git" GIT_WORK_TREE="$fixture_directory" \
    GIT_INDEX_FILE="$fixture_directory/.git/index" \
    "$hook_path" > temporary/output 2>&1 || hook_status=$?
}

assert_index_preserved() {
  [ "$(git write-tree)" = "$original_index" ] || fail 'original index changed'
}

assert_calls() {
  printf '%s\n' "$@" > temporary/expected-calls
  cmp temporary/expected-calls temporary/calls || fail 'unexpected Maven commands'
}

test_success_preserves_partial_staging_and_untracked_files() {
  arrange_repository partial
  printf 'unstaged\n' > source.txt
  printf 'private\n' > private.txt

  act_run_hook

  [ "$hook_status" -eq 0 ] || fail 'partial staging validation failed'
  assert_index_preserved
  [ "$(cat source.txt)" = unstaged ] || fail 'unstaged content changed'
  [ "$(cat private.txt)" = private ] || fail 'untracked content changed'
  assert_calls rewrite:run spotless:apply spotless:check
}

test_command_failure_stops_following_commands() {
  arrange_repository "failure-$1"
  export FAIL_COMMAND="$1"

  act_run_hook

  [ "$hook_status" -eq 42 ] || fail 'command failure was hidden'
  assert_index_preserved
  [ "$(cat source.txt)" = staged ] || fail 'working file changed'
  case "$1" in
    rewrite:run) assert_calls rewrite:run ;;
    spotless:apply) assert_calls rewrite:run spotless:apply ;;
    spotless:check) assert_calls rewrite:run spotless:apply spotless:check ;;
  esac
}

test_formatter_changes_produce_patch() {
  arrange_repository formatter
  export FORMAT_COMMAND=spotless:check

  act_run_hook

  [ "$hook_status" -ne 0 ] || fail 'formatter changes were accepted'
  assert_index_preserved
  [ "$(cat source.txt)" = staged ] || fail 'formatter changed original file'
  set -- temporary/arena-pre-commit-changes.*
  [ "$#" -eq 1 ] && [ -f "$1" ] || fail 'review patch missing'
  git apply --check "$1" || fail 'review patch cannot be applied'
  grep -q '^+formatted$' "$1" || fail 'review patch lacks formatted content'
}

test_alternate_index_is_respected() {
  arrange_repository alternate-index
  cp .git/index temporary/alternate-index
  printf 'different-index-content\n' > source.txt
  git add source.txt
  original_index=$(git write-tree)

  hook_status=0
  GIT_INDEX_FILE="$fixture_directory/temporary/alternate-index" \
    "$hook_path" > temporary/output 2>&1 || hook_status=$?

  [ "$hook_status" -eq 0 ] || fail 'alternate index was ignored'
  assert_index_preserved
  [ "$(cat source.txt)" = different-index-content ] || fail 'working file changed'
  [ "$(GIT_INDEX_FILE=temporary/alternate-index git show :source.txt)" = staged ] \
    || fail 'alternate index changed'
  assert_calls rewrite:run spotless:apply spotless:check
}

test_failed_rewrite_preserves_patch() {
  arrange_repository failed-rewrite-patch
  export FORMAT_COMMAND=rewrite:run FAIL_COMMAND=rewrite:run

  act_run_hook

  [ "$hook_status" -ne 0 ] || fail 'failed rewriting accepted'
  assert_index_preserved
  [ "$(cat source.txt)" = staged ] || fail 'failed rewrite changed original file'
  set -- temporary/arena-pre-commit-changes.*
  [ "$#" -eq 1 ] && [ -f "$1" ] || fail 'failed rewrite patch missing'
  git apply --check "$1" || fail 'failed rewrite patch cannot be applied'
  assert_calls rewrite:run
}

test_commit_contains_only_staged_content() {
  arrange_repository commit
  printf 'unstaged\n' > source.txt
  printf 'private\n' > private.txt

  git commit --quiet -m 'test: preserve staged content' > temporary/output 2>&1

  [ "$(git show HEAD:source.txt)" = staged ] || fail 'commit includes unstaged changes'
  [ "$(cat source.txt)" = unstaged ] || fail 'commit changed unstaged content'
  [ "$(cat private.txt)" = private ] || fail 'commit changed untracked content'
  assert_index_preserved
  assert_calls rewrite:run spotless:apply spotless:check
}

test_success_preserves_partial_staging_and_untracked_files
test_command_failure_stops_following_commands rewrite:run
test_command_failure_stops_following_commands spotless:apply
test_command_failure_stops_following_commands spotless:check
test_formatter_changes_produce_patch
test_alternate_index_is_respected
test_failed_rewrite_preserves_patch
test_commit_contains_only_staged_content
echo 'PASS: all eight isolated pre-commit regression cases'
