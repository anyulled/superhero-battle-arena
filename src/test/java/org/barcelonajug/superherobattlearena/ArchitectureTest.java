package org.barcelonajug.superherobattlearena;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "org.barcelonajug.superherobattlearena", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

        @ArchTest
        public static final ArchRule layer_dependencies_are_respected = layeredArchitecture()
                        .consideringOnlyDependenciesInAnyPackage("org.barcelonajug.superherobattlearena..")
                        .layer("Domain").definedBy("..domain..")
                        .layer("Service").definedBy("..service..")
                        .layer("Web").definedBy("..web..")
                        .layer("Infrastructure").definedBy("..infrastructure..")
                        .withOptionalLayers(true)

                        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Service", "Web", "Infrastructure")
                        .whereLayer("Service").mayOnlyBeAccessedByLayers("Web")
                        .whereLayer("Web").mayNotBeAccessedByAnyLayer()
                        .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                        .ignoreDependency(
                                        org.barcelonajug.superherobattlearena.SuperheroBattleArenaApplication.class,
                                        org.barcelonajug.superherobattlearena.SuperheroBattleArenaApplication.class); // Allow
                                                                                                                      // main
                                                                                                                      // class
                                                                                                                      // to
                                                                                                                      // do
                                                                                                                      // whatever
                                                                                                                      // (bootstrapping)
                                                                                                                      // or
                                                                                                                      // refine
                                                                                                                      // later
}
