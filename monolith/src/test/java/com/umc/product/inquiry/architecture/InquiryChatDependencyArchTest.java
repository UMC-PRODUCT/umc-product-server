package com.umc.product.inquiry.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

@DisplayName("Chat과 Inquiry의 의존 경계")
class InquiryChatDependencyArchTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("com.umc.product.chat", "com.umc.product.inquiry");

    @Test
    @DisplayName("Chat은 Inquiry를 참조하지 않는다")
    void chatDoesNotDependOnInquiry() {
        noClasses().that().resideInAPackage("com.umc.product.chat..")
            .should().dependOnClassesThat().resideInAPackage("com.umc.product.inquiry..")
            .check(CLASSES);
    }

    @Test
    @DisplayName("Inquiry는 Chat의 공개 UseCase와 DTO, enum만 참조한다")
    void inquiryUsesOnlyChatPublicContracts() {
        noClasses().that().resideInAPackage("com.umc.product.inquiry..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.umc.product.chat.adapter..",
                "com.umc.product.chat.application.service..",
                "com.umc.product.chat.application.policy..",
                "com.umc.product.chat.application.port.out.."
            )
            .orShould().dependOnClassesThat().haveNameMatching(
                "com\\.umc\\.product\\.chat\\.domain\\."
                    + "(ChatRoom|ChatMember|ChatMessage|ChatMessageMention|ChatMessageReaction)"
            )
            .check(CLASSES);
    }
}
