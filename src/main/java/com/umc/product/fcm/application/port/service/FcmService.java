package com.umc.product.fcm.application.port.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.umc.product.fcm.application.port.in.ManageFcmUseCase;
import com.umc.product.fcm.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.fcm.application.port.in.NotificationCommand;
import com.umc.product.fcm.application.port.out.LoadFcmPort;
import com.umc.product.fcm.application.port.out.SaveFcmPort;
import com.umc.product.fcm.entity.FCMToken;
import com.umc.product.fcm.entity.exception.FcmErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberErrorCode;
import jakarta.transaction.Transactional;
import java.util.Optional;
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
    public void registerFCMToken(Long userId, FcmRegistrationRequest request) {
        Member member = loadMemberPort.findById(userId)
                .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        // 이미 등록된 FCMToken이 있는지 확인
        Optional<FCMToken> existingToken = loadFcmPort.findByMemberId(userId);

        if (existingToken.isPresent()) {
            // 이미 등록된 FCMToken이 있는 경우 값을 업데이트
            existingToken.get().updateToken(request.fcmToken());
        } else {
            // 등록된 FCMToken이 없는 경우 새로 생성하여 저장
            FCMToken newToken = FCMToken.createFCMToken(member, request.fcmToken());
            saveFcmPort.save(newToken);
        }
    }

    @Override
    public void sendMessageByToken(NotificationCommand command) {
        Member member = loadMemberPort.findById(command.memberId())
                .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        FCMToken fcm = loadFcmPort.findByMemberId(member.getId())
                .orElseThrow(() -> new BusinessException(Domain.FCM, FcmErrorCode.USER_FCM_NOT_FOUND));

        String fcmToken = fcm.getFcmToken();

        if (!fcmToken.isEmpty()) {
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

    private static Message getMessage(NotificationCommand command, String fcmToken) {
        Notification notification = Notification.builder().setTitle(command.title()).setBody(command.body()).build();

        Message message = Message.builder().setToken(fcmToken).setNotification(notification).build();
        return message;
    }
}
