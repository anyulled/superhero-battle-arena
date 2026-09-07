# Dependency security checks

Pull requests run dependency review without repository secrets. Trusted main pushes and scheduled runs execute Snyk plus OWASP Dependency-Check 13.0.0. The latter checks both NVD and Sonatype Guide, replacing the standalone OSS Index Maven plugin that could report success after an HTTP failure.

The combined scan fails for vulnerabilities with a CVSS score of 7 or higher, missing credentials, and remote service errors. Guide caching is disabled so an old cached result cannot hide a current authentication failure. Reports are uploaded as the `dependency-audit-reports` artifact.

Configure `GUIDE_API_TOKEN` and `NVD_API_KEY` as GitHub Actions repository secrets. Use a Sonatype Guide personal access token; do not paste it into an issue or commit it. The Maven settings template resolves the token from the environment. Its `guide` username is a placeholder supported by Guide token authentication. The NVD key is also read from the environment rather than passed as a command argument.

With those variables securely supplied in the environment, run `sh scripts/security-audit.sh`. Missing variables stop execution before Maven starts. Local regression tests use a mock Maven process and do not authenticate with either service; a passing test suite is not evidence of a successful live vulnerability scan.

Configuration references: [Dependency-Check Maven options](https://dependency-check.github.io/DependencyCheck/dependency-check-maven/configuration.html), [Guide token authentication](https://help.sonatype.com/en/using-guide-personal-access-tokens-with-oss-index-api-integrations.html), and [OSS Index migration](https://help.sonatype.com/en/oss-index-migration-steps.html).
