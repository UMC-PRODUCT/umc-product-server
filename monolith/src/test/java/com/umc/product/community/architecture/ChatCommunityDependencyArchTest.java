package com.umc.product.community.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

@DisplayName("Chat과 Community의 의존 경계")
class ChatCommunityDependencyArchTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("com.umc.product.chat", "com.umc.product.community");

    @Test
    @DisplayName("Chat은 Community 구현을 참조하지 않는다")
    void chatDoesNotDependOnCommunity() {
        noClasses().that().resideInAPackage("com.umc.product.chat..")
            .should().dependOnClassesThat().resideInAPackage("com.umc.product.community..")
            .check(CLASSES);
    }

    @Test
    @DisplayName("Community 도메인 모델은 Chat 구현을 참조하지 않는다")
    void communityDomainDoesNotDependOnChat() {
        noClasses().that().resideInAPackage("com.umc.product.community.domain..")
            .should().dependOnClassesThat().resideInAPackage("com.umc.product.chat..")
            .check(CLASSES);
    }

    @Test
    @DisplayName("Community는 Chat의 어댑터와 내부 구현 및 아웃바운드 포트를 참조하지 않는다")
    void communityUsesOnlyChatPublicContracts() {
        noClasses().that().resideInAPackage("com.umc.product.community..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.umc.product.chat.adapter..",
                "com.umc.product.chat.application.service..",
                "com.umc.product.chat.application.policy..",
                "com.umc.product.chat.application.port.out.."
            )
            .check(CLASSES);
    }

    @Test
    @DisplayName("Community는 Chat 엔티티를 직접 참조하지 않는다")
    void communityDoesNotDependOnChatEntities() {
        noClasses().that().resideInAPackage("com.umc.product.community..")
            .should().dependOnClassesThat().haveNameMatching(
                "com\\.umc\\.product\\.chat\\.domain\\."
                    + "(ChatRoom|ChatMember|ChatMessage|ChatMessageMention|ChatMessageReaction)"
            )
            .check(CLASSES);
    }
}
