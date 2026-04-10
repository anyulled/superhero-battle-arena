#!/bin/bash
mvn clean jacoco:prepare-agent test jacoco:report -Dtest=MatchUseCaseTest
grep -A 5 -B 5 "matchesToSave.isEmpty()" src/main/java/org/barcelonajug/superherobattlearena/application/usecase/MatchUseCase.java
