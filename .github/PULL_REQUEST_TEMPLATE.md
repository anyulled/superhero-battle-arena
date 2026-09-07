## Description
<!-- Describe your changes in detail here. Explain the motivation for why these changes are needed. -->

## Impact
<!-- What components or systems are affected? Does this involve a breaking change? Does it introduce new dependencies? -->

## Testing
<!-- Describe how you verified your changes. Have you added or updated unit/integration tests? Include instructions so reviewers can test them. -->
- [ ] Added unit tests
- [ ] Added integration tests
- [ ] Tested manually

## Checklist

- [ ] My code follows the code style (Spotless) and architectural rules (`ARCHITECTURE.md` / `AGENTS.md`) of this project
- [ ] I have executed `./mvnw clean verify` locally and all tests and linting passed
- [ ] I have kept all five core coverage metrics at or above 90% and reviewed adapter coverage separately
- [ ] I have recorded exact validation commands and results, including the separate adapter coverage report
- [ ] I have added/updated Javadoc or OpenAPI spec where appropriate
- [ ] I have updated the documentation accordingly (README, ADRs, etc.)
- [ ] Any OpenAPI fixture change is intentional and its contract diff has been reviewed
