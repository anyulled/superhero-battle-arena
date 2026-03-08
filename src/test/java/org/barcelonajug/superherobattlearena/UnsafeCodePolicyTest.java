package org.barcelonajug.superherobattlearena;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@AnalyzeClasses(
    packages = "org.barcelonajug.superherobattlearena",
    importOptions = ImportOption.DoNotIncludeTests.class)
public class UnsafeCodePolicyTest {

  /** No native methods allowed */
  @ArchTest
  static final ArchRule no_native_methods =
      methods()
          .should()
          .notHaveModifier(JavaModifier.NATIVE)
          .as("Native methods are strictly forbidden.");

  /** No usage of sun.misc.Unsafe */
  @ArchTest
  static final ArchRule no_usage_of_sun_misc_unsafe =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("sun.misc..")
          .as("Using sun.misc.Unsafe is strictly forbidden.");

  /** No bypassing visibility with reflection */
  @ArchTest
  static final ArchRule no_bypassing_visibility_with_reflection =
      noClasses()
          .should()
          .callMethod(AccessibleObject.class, "setAccessible", boolean.class)
          .orShould()
          .callMethod(Field.class, "setAccessible", boolean.class)
          .orShould()
          .callMethod(Method.class, "setAccessible", boolean.class)
          .orShould()
          .callMethod(Constructor.class, "setAccessible", boolean.class)
          .as("Bypassing visibility with setAccessible(true) is strictly forbidden.");
}
