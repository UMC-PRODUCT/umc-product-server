package com.umc.product.notification.application.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import com.umc.product.notification.application.port.in.dto.TopicNotificationCommand;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.exception.FcmDomainException;
import com.umc.product.notification.domain.exception.FcmErrorCode;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService implements ManageFcmUseCase {

    private final FirebaseMessaging firebaseMessaging;
    private final LoadMemberPort loadMemberPort;
    private final LoadFcmPort loadFcmPort;
    private final SaveFcmPort saveFcmPort;

    @Override
    @Transactional
    public void registerFcmToken(Long userId, FcmRegistrationRequest request) {
        Member member = loadMemberPort.findById(userId)
                .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        // 이미 등록된 FCMToken이 있는지 확인
        FcmToken existingToken = loadFcmPort.findByMemberId(userId);

        if (existingToken != null) {
            // 이미 등록된 FCMToken이 있는 경우 값을 업데이트
            existingToken.updateToken(request.fcmToken());
        } else {
            // 등록된 FCMToken이 없는 경우 새로 생성하여 저장
            FcmToken newToken = FcmToken.createFCMToken(member, request.fcmToken());
            saveFcmPort.save(newToken);
        }
    }

    @Override
    public void sendMessageByToken(NotificationCommand command) {
        Member member = loadMemberPort.findById(command.memberId())
                .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        FcmToken fcm = loadFcmPort.findByMemberId(member.getId());

        String fcmToken = fcm.getFcmToken();

        if (!fcmToken.isBlank()) {
            Message message = getMessage(command, fcmToken);

            try {
                firebaseMessaging.send(message);
                log.info("푸시 알림 전송 완료 userId = {}", member.getId());
            } catch (FirebaseMessagingException e) {
                log.error("푸시 알림 전송 실패 userId = {}", member.getId(), e);
                throw new BusinessException(Domain.FCM, FcmErrorCode.USER_FCM_NOT_FOUND);
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

    private static Message getMessage(NotificationCommand command, String fcmToken) {
        Notification notification = Notification.builder().setTitle(command.title()).setBody(command.body()).build();

        Message message = Message.builder().setToken(fcmToken).setNotification(notification).build();
        return message;
    }

}
