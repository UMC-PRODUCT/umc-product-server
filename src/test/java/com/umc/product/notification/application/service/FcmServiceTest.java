package com.umc.product.notification.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.TopicManagementResponse;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import com.umc.product.notification.application.port.in.dto.TopicNotificationCommand;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.exception.FcmDomainException;
import com.umc.product.global.exception.BusinessException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

    @Mock FirebaseMessaging firebaseMessaging;
    @Mock LoadMemberPort loadMemberPort;
    @Mock LoadFcmPort loadFcmPort;
    @Mock SaveFcmPort saveFcmPort;

    @InjectMocks FcmService sut;

    // -- test fixtures --

    private Member createMember(Long id) {
        Member member = Member.builder()
                .name("강하나")
                .nickname("와나")
                .email("test@umc.com")
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private FcmToken createFcmToken(String token) {
        try {
            Constructor<FcmToken> constructor = FcmToken.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FcmToken fcmToken = constructor.newInstance();
            ReflectionTestUtils.setField(fcmToken, "fcmToken", token);
            return fcmToken;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class registerFcmToken {

        @Test
        void 기존_토큰이_없으면_새로_생성하여_저장한다() {
            // given
            Member member = createMember(1L);
            given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));
            given(loadFcmPort.findByMemberId(1L)).willReturn(null);

            // when
            sut.registerFcmToken(1L, new FcmRegistrationRequest("new-token"));

            // then
            then(saveFcmPort).should().save(any(FcmToken.class));
        }

        @Test
        void 기존_토큰이_있으면_업데이트한다() {
            // given
            Member member = createMember(1L);
            given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

            FcmToken existingToken = createFcmToken("old-token");
            given(loadFcmPort.findByMemberId(1L)).willReturn(existingToken);

            // when
            sut.registerFcmToken(1L, new FcmRegistrationRequest("new-token"));

            // then: 기존 토큰이 업데이트되므로 save는 호출되지 않음
            then(saveFcmPort).should(never()).save(any());
        }

        @Test
        void 존재하지_않는_회원이면_예외를_던진다() {
            // given
            given(loadMemberPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    sut.registerFcmToken(999L, new FcmRegistrationRequest("token")))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    class subscribeToTopic {

        @Test
        void 정상적으로_토픽을_구독한다() throws FirebaseMessagingException {
            // given
            TopicManagementResponse response = mock(TopicManagementResponse.class);
            given(firebaseMessaging.subscribeToTopic(List.of("token"), "gisu-1"))
                    .willReturn(response);

            // when
            sut.subscribeToTopic(List.of("token"), "gisu-1");

            // then
            then(firebaseMessaging).should().subscribeToTopic(List.of("token"), "gisu-1");
        }

        @Test
        void 토큰_리스트가_null이면_아무것도_호출하지_않는다() {
            // when
            sut.subscribeToTopic(null, "gisu-1");

            // then
            then(firebaseMessaging).shouldHaveNoInteractions();
        }

        @Test
        void 토큰_리스트가_비어있으면_아무것도_호출하지_않는다() {
            // when
            sut.subscribeToTopic(List.of(), "gisu-1");

            // then
            then(firebaseMessaging).shouldHaveNoInteractions();
        }

        @Test
        void Firebase_예외_발생시_FcmDomainException을_던진다() throws FirebaseMessagingException {
            // given
            given(firebaseMessaging.subscribeToTopic(any(), any()))
                    .willThrow(FirebaseMessagingException.class);

            // when & then
            assertThatThrownBy(() ->
                    sut.subscribeToTopic(List.of("token"), "gisu-1"))
                    .isInstanceOf(FcmDomainException.class);
        }
    }

    @Nested
    class unsubscribeFromTopic {

        @Test
        void 정상적으로_토픽_구독을_해제한다() throws FirebaseMessagingException {
            // given
            TopicManagementResponse response = mock(TopicManagementResponse.class);
            given(firebaseMessaging.unsubscribeFromTopic(List.of("token"), "gisu-1"))
                    .willReturn(response);

            // when
            sut.unsubscribeFromTopic(List.of("token"), "gisu-1");

            // then
            then(firebaseMessaging).should().unsubscribeFromTopic(List.of("token"), "gisu-1");
        }

        @Test
        void 토큰_리스트가_null이면_아무것도_호출하지_않는다() {
            // when
            sut.unsubscribeFromTopic(null, "gisu-1");

            // then
            then(firebaseMessaging).shouldHaveNoInteractions();
        }

        @Test
        void Firebase_예외_발생시_FcmDomainException을_던진다() throws FirebaseMessagingException {
            // given
            given(firebaseMessaging.unsubscribeFromTopic(any(), any()))
                    .willThrow(FirebaseMessagingException.class);

            // when & then
            assertThatThrownBy(() ->
                    sut.unsubscribeFromTopic(List.of("token"), "gisu-1"))
                    .isInstanceOf(FcmDomainException.class);
        }
    }

    @Nested
    class sendMessageByTopic {

        @Test
        void 정상적으로_토픽_메시지를_전송한다() throws FirebaseMessagingException {
            // given
            given(firebaseMessaging.send(any(Message.class))).willReturn("message-id");

            // when
            sut.sendMessageByTopic(new TopicNotificationCommand("gisu-1", "제목", "내용"));

            // then
            then(firebaseMessaging).should().send(any(Message.class));
        }

        @Test
        void Firebase_예외_발생시_FcmDomainException을_던진다() throws FirebaseMessagingException {
            // given
            given(firebaseMessaging.send(any(Message.class)))
                    .willThrow(FirebaseMessagingException.class);

            // when & then
            assertThatThrownBy(() ->
                    sut.sendMessageByTopic(new TopicNotificationCommand("gisu-1", "제목", "내용")))
                    .isInstanceOf(FcmDomainException.class);
        }
    }

    @Nested
    class sendMessageByToken {

        @Test
        void 정상적으로_개별_메시지를_전송한다() throws FirebaseMessagingException {
            // given
            Member member = createMember(1L);
            given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

            FcmToken token = createFcmToken("test-token");
            given(loadFcmPort.findByMemberId(1L)).willReturn(token);

            given(firebaseMessaging.send(any(Message.class))).willReturn("message-id");

            // when
            sut.sendMessageByToken(new NotificationCommand(1L, "제목", "내용"));

            // then
            then(firebaseMessaging).should().send(any(Message.class));
        }

        @Test
        void 존재하지_않는_회원이면_예외를_던진다() {
            // given
            given(loadMemberPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    sut.sendMessageByToken(new NotificationCommand(999L, "제목", "내용")))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
