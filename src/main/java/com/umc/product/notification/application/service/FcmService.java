package com.umc.product.notification.application.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import com.umc.product.notification.application.port.in.dto.TopicNotificationCommand;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveFcmOutboxPort;
import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmOutbox;
import com.umc.product.notification.domain.FcmOutboxEvent;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.exception.FcmDomainException;
import com.umc.product.notification.domain.exception.FcmErrorCode;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService implements ManageFcmUseCase {

    private final GetMemberUseCase getMemberUseCase;

    private final FirebaseMessaging firebaseMessaging;
    private final LoadFcmPort loadFcmPort;
    private final SaveFcmPort saveFcmPort;
    private final SaveFcmOutboxPort saveFcmOutboxPort;
    private final ApplicationEventPublisher eventPublisher;

    private static Message getMessage(NotificationCommand command, String fcmToken) {
        Notification notification = Notification.builder()
            .setTitle(command.title())
            .setBody(command.body())
            .build();

        log.trace("FCM 메시지 생성: token={}, title={}, body={}", fcmToken, command.title(), command.body());
        Message message = Message.builder().setToken(fcmToken).setNotification(notification).build();
        return message;
    }

    @Override
    @Transactional
    public void registerFcmToken(Long userId, FcmRegistrationRequest request) {
        String[] oldToken = {null};

        loadFcmPort.findOptionalByMemberId(userId)
            .ifPresentOrElse(
                existingToken -> {
                    oldToken[0] = existingToken.getFcmToken();
                    existingToken.updateToken(request.fcmToken());
                },
                () -> saveFcmPort.save(FcmToken.createFCMToken(userId, request.fcmToken()))
            );

        if (oldToken[0] != null) {
            saveFcmOutboxPort.save(FcmOutbox.unsubscribeEvent(userId, oldToken[0]));
        }
        saveFcmOutboxPort.save(FcmOutbox.subscribeEvent(userId));
        eventPublisher.publishEvent(new FcmOutboxEvent());
    }

    @Override
    public void sendMessageByToken(NotificationCommand command) {

        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(command.memberId());

        FcmToken fcm = loadFcmPort.findByMemberId(memberInfo.id());

        String fcmToken = fcm.getFcmToken();

        if (!fcmToken.isBlank()) {
            Message message = getMessage(command, fcmToken);

            try {
                firebaseMessaging.send(message);
                log.info("푸시 알림 전송 완료 userId = {}", memberInfo.id());
            } catch (FirebaseMessagingException e) {
                log.error("푸시 알림 전송 실패 userId = {}", memberInfo.id(), e);
                throw new FcmDomainException(FcmErrorCode.USER_FCM_NOT_FOUND);
            }
        }
    }

    @Override
    public void subscribeToTopic(List<String> fcmTokens, String topic) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(fcmTokens, topic);
            log.info("토픽 구독 완료 topic={}, 성공={}, 실패={}",
                topic, response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("토픽 구독 실패 topic={}", topic, e);
            throw new FcmDomainException(FcmErrorCode.TOPIC_SUBSCRIBE_FAILED);
        }
    }

    @Override
    public void unsubscribeFromTopic(List<String> fcmTokens, String topic) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(fcmTokens, topic);
            log.info("토픽 구독 해제 완료 topic={}, 성공={}, 실패={}",
                topic, response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("토픽 구독 해제 실패 topic={}", topic, e);
            throw new FcmDomainException(FcmErrorCode.TOPIC_UNSUBSCRIBE_FAILED);
        }
    }

    @Override
    public void sendMessageByTopic(TopicNotificationCommand command) {
        Notification notification = Notification.builder()
            .setTitle(command.title())
            .setBody(command.body())
            .build();

        Message message = Message.builder()
            .setTopic(command.topic())
            .setNotification(notification)
            .build();

        try {
            firebaseMessaging.send(message);
            log.info("토픽 메시지 전송 완료 topic={}", command.topic());
        } catch (FirebaseMessagingException e) {
            log.error("토픽 메시지 전송 실패 topic={}", command.topic(), e);
            throw new FcmDomainException(FcmErrorCode.TOPIC_SEND_FAILED);
        }
    }

}
