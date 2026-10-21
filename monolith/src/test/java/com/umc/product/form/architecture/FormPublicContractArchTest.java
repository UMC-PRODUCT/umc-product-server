package com.umc.product.form.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

@DisplayName("Form 공개 계약 의존 경계")
class FormPublicContractArchTest {

    private static final String FORM_PACKAGE = "com.umc.product.form.";
    private static final String FORM_PORT_IN_PACKAGE = FORM_PACKAGE + "application.port.in.";
    private static final String PROJECT_SEED_FACTORY =
        "com.umc.product.test.application.service.DummyApplicationFormFactory";
    private static final List<String> PRODUCT_CONSUMER_PACKAGES = List.of(
        "com.umc.product.project.",
        "com.umc.product.recruiting.",
        "com.umc.product.notice.",
        "com.umc.product.feedback."
    );
    private static final Set<String> PUBLIC_ENUMS = Set.of(
        FORM_PACKAGE + "domain.enums.QuestionType",
        FORM_PACKAGE + "domain.enums.FormStatus",
        FORM_PACKAGE + "domain.enums.FormResponseStatus"
    );
    private static final JavaClasses CLASSES = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("com.umc.product");

    private static final DescribedPredicate<JavaClass> FORM_CONSUMERS =
        new DescribedPredicate<>("Form을 제외한 운영 코드") {
            @Override
            public boolean test(JavaClass source) {
                String name = source.getName();
                return name.startsWith("com.umc.product.")
                    && !name.startsWith(FORM_PACKAGE);
            }
        };

    private static final DescribedPredicate<JavaClass> UNLISTED_FORM_CONSUMERS =
        new DescribedPredicate<>("목록에 없는 Form 소비 코드") {
            @Override
            public boolean test(JavaClass source) {
                String name = source.getName();
                return name.startsWith("com.umc.product.")
                    && !name.startsWith(FORM_PACKAGE)
                    && PRODUCT_CONSUMER_PACKAGES.stream().noneMatch(name::startsWith)
                    && !name.equals(PROJECT_SEED_FACTORY);
            }
        };

    private static final DescribedPredicate<JavaClass> FORM_INTERNAL_TYPE =
        new DescribedPredicate<>("Form 공개 계약 밖의 타입") {
            @Override
            public boolean test(JavaClass target) {
                String name = target.getName();
                return name.startsWith(FORM_PACKAGE)
                    && !name.startsWith(FORM_PORT_IN_PACKAGE)
                    && !PUBLIC_ENUMS.contains(name);
            }
        };

    @Test
    @DisplayName("Form 외 운영 코드는 Form 공개 계약만 참조한다")
    void consumersUseOnlyFormPublicContract() {
        noClasses().that(FORM_CONSUMERS)
            .should().dependOnClassesThat(FORM_INTERNAL_TYPE)
            .check(CLASSES);
    }

    @Test
    @DisplayName("목록에 없는 도메인은 Form을 참조하지 않는다")
    void otherDomainsDoNotDependOnForm() {
        noClasses().that(UNLISTED_FORM_CONSUMERS)
            .should().dependOnClassesThat().resideInAPackage("com.umc.product.form..")
            .check(CLASSES);
    }

    @Test
    @DisplayName("Form 공개 계약은 Form 내부 구현을 참조하지 않는다")
    void formPublicContractDoesNotDependOnImplementation() {
        noClasses().that().resideInAPackage("com.umc.product.form.application.port.in..")
            .should().dependOnClassesThat(FORM_INTERNAL_TYPE)
            .check(CLASSES);
    }
}
