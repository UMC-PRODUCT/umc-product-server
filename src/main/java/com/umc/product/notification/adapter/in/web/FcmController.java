package com.umc.product.notification.adapter.in.web;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.adapter.in.web.dto.request.FcmTestSendRequest;
import com.umc.product.notification.application.port.in.ManageFcmTopicUseCase;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification/fcm")
@RequiredArgsConstructor
public class FcmController implements FcmControllerApi {

    private final ManageFcmUseCase manageFcmUseCase;
    private final ManageFcmTopicUseCase manageFcmTopicUseCase;

    @Override
    @PostMapping("/{memberId}")
    public void registerFcmToken(
            // TODO: 인증 적용 시 @PathVariable -> @AuthenticationPrincipal 변경 필요
            @PathVariable("memberId") Long userId,
            @RequestBody FcmRegistrationRequest request) {
        // 토큰 갱신 시 이전 토큰에 연결된 토픽 구독을 먼저 해제
        manageFcmTopicUseCase.unsubscribeAllTopicsByMemberId(userId);
        // 토큰 등록 또는 업데이트
        manageFcmUseCase.registerFcmToken(userId, request);
        // 새 토큰으로 토픽 재구독
        manageFcmTopicUseCase.subscribeAllTopicsByMemberId(userId);
    }

    @Override
    @PostMapping("/test-send")
    public void sendTestNotification(@RequestBody FcmTestSendRequest request) {
        manageFcmUseCase.sendMessageByToken(request.toCommand());
    }
}
