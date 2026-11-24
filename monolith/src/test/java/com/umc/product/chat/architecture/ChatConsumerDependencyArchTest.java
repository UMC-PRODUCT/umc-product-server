package com.umc.product.chat.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.umc.product.chat.application.port.in.query.GetChatMessageRoomUseCase;
import com.umc.product.chat.application.port.in.query.GetChatMessageUseCase;
import com.umc.product.chat.application.port.in.query.GetChatMessagesUseCase;

/**
 * Chat 엔진과 그 소비 도메인(Chat 방을 자기 리소스로 감싸는 도메인) 사이의 경계. 새 소비 도메인은 {@code CONSUMERS}에 추가한다.
 */
@DisplayName("Chat과 소비 도메인의 의존 경계")
class ChatConsumerDependencyArchTest {

    private static final String CHAT = "com.umc.product.chat..";
    private static final List<String> CONSUMERS = List.of("community", "inquiry");
    private static final JavaClasses CLASSES = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("com.umc.product");

    @ParameterizedTest(name = "{0}")
    @MethodSource("consumers")
    @DisplayName("Chat은 소비 도메인을 참조하지 않는다")
    void chatDoesNotDependOnConsumer(String consumer) {
        noClasses().that().resideInAPackage(CHAT)
            .should().dependOnClassesThat().resideInAPackage(packageOf(consumer))
            .check(CLASSES);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("consumers")
    @DisplayName("소비 도메인의 도메인 모델은 Chat을 참조하지 않는다")
    void consumerDomainDoesNotDependOnChat(String consumer) {
        noClasses().that().resideInAPackage("com.umc.product." + consumer + ".domain..")
            .should().dependOnClassesThat().resideInAPackage(CHAT)
            .check(CLASSES);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("consumers")
    @DisplayName("소비 도메인은 Chat의 공개 UseCase와 DTO, enum만 참조한다")
    void consumerUsesOnlyChatPublicContracts(String consumer) {
        noClasses().that().resideInAPackage(packageOf(consumer))
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

    // 문의 방은 Chat 조회 범위 PUBLIC 이라 엔진 조회만으로는 막히지 않는다. 메시지 조회는 리소스 권한을 확인하는
    // 소비 도메인만 부를 수 있어야 한다.
    @Test
    @DisplayName("Chat 메시지 조회 UseCase는 Chat과 소비 도메인만 참조한다")
    void onlyConsumersReadChatMessages() {
        String[] allowed = Stream.concat(
            Stream.of(CHAT),
            CONSUMERS.stream().map(ChatConsumerDependencyArchTest::packageOf)
        ).toArray(String[]::new);
        noClasses().that().resideOutsideOfPackages(allowed)
            .should().dependOnClassesThat().belongToAnyOf(
                GetChatMessagesUseCase.class,
                GetChatMessageUseCase.class,
                GetChatMessageRoomUseCase.class
            )
            .check(CLASSES);
    }

    static List<String> consumers() {
        return CONSUMERS;
    }

    private static String packageOf(String consumer) {
        return "com.umc.product." + consumer + "..";
    }
}
