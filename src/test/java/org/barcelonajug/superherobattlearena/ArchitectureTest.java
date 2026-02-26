package org.barcelonajug.superherobattlearena;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(
    packages = "org.barcelonajug.superherobattlearena",
    importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

  private static final String DOMAIN = "org.barcelonajug.superherobattlearena.domain..";
  private static final String APPLICATION = "org.barcelonajug.superherobattlearena.application..";
  private static final String ADAPTER = "org.barcelonajug.superherobattlearena.adapter..";
  private static final String CONFIG = "org.barcelonajug.superherobattlearena.config..";

  // Sub-packages
  private static final String PORT_IN =
      "org.barcelonajug.superherobattlearena.application.port.in..";
  private static final String PORT_OUT =
      "org.barcelonajug.superherobattlearena.application.port.out..";
  private static final String ADAPTER_IN_WEB =
      "org.barcelonajug.superherobattlearena.adapter.in.web..";
  private static final String ADAPTER_OUT_PERSISTENCE =
      "org.barcelonajug.superherobattlearena.adapter.out.persistence..";

  /** No dependency cycles between top-level slices */
  @ArchTest
  static final ArchRule no_cycles_between_top_level_slices =
      slices()
          .matching("org.barcelonajug.superherobattlearena.(*)..")
          .should()
          .beFreeOfCycles()
          .allowEmptyShould(true);

  /** Entity only in persistence adapter, never in domain or application */
  @ArchTest
  static final ArchRule no_entities_in_domain_or_application =
      noClasses()
          .that()
          .resideInAnyPackage(DOMAIN, APPLICATION)
          .should()
          .beAnnotatedWith(Entity.class)
          .allowEmptyShould(true);

  /** Ports are interfaces */
  @ArchTest
  static final ArchRule ports_are_interfaces =
      classes()
          .that()
          .resideInAnyPackage(PORT_IN, PORT_OUT)
          .should()
          .beInterfaces()
          .allowEmptyShould(true);

  /** Controllers only in adapter.in.web */
  @ArchTest
  static final ArchRule controllers_are_only_in_web_adapter =
      classes()
          .that()
          .areAnnotatedWith(Controller.class)
          .or()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAnyPackage(ADAPTER_IN_WEB)
          .allowEmptyShould(true);

  /** Persistence adapter cannot depend on web or input ports */
  @ArchTest
  static final ArchRule persistence_does_not_depend_on_web_or_input_ports =
      noClasses()
          .that()
          .resideInAnyPackage(ADAPTER_OUT_PERSISTENCE)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(ADAPTER_IN_WEB, PORT_IN)
          .allowEmptyShould(true);

  /** Entity only in persistence adapter */
  @ArchTest
  static final ArchRule entities_are_only_in_persistence_adapter =
      classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .should()
          .resideInAnyPackage(ADAPTER_OUT_PERSISTENCE)
          .allowEmptyShould(true);

  /** Repository only in adapter.out.persistence (Spring Data / DAOs) */
  @ArchTest
  static final ArchRule repositories_are_only_in_persistence_adapter =
      classes()
          .that()
          .areAnnotatedWith(Repository.class)
          .should()
          .resideInAnyPackage(ADAPTER_OUT_PERSISTENCE)
          .allowEmptyShould(true);

  /** Application layer cannot depend on adapters or config */
  @ArchTest
  static final ArchRule application_does_not_depend_on_adapters_or_config =
      noClasses()
          .that()
          .resideInAnyPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(ADAPTER, CONFIG)
          .allowEmptyShould(true);

  private static final String SPRING = "..org.springframework..";
  private static final String SPRING_DATA = "..org.springframework.data..";
  private static final String JPA = "..jakarta.persistence..";

  /** Domain is pure: isolated from external layers and frameworks */
  @ArchTest
  static final ArchRule domain_is_pure =
      noClasses()
          .that()
          .resideInAnyPackage(DOMAIN)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(ADAPTER, APPLICATION, CONFIG, SPRING, SPRING_DATA, JPA)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule domain_should_not_depend_on_application_or_adapter =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(APPLICATION, ADAPTER)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule application_should_not_depend_on_adapter =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(ADAPTER)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule web_adapters_should_not_depend_on_ports =
      noClasses()
          .that()
          .resideInAPackage(ADAPTER_IN_WEB)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(PORT_IN, PORT_OUT)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule use_cases_should_end_with_UseCase =
      classes()
          .that()
          .resideInAPackage(APPLICATION + "usecase")
          .should()
          .haveSimpleNameEndingWith("UseCase")
          .allowEmptyShould(true);
}
