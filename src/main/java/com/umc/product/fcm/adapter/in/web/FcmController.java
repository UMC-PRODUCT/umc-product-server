package com.umc.product.fcm.adapter.in.web;

import com.umc.product.fcm.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.fcm.adapter.in.web.dto.request.FcmTestSendRequest;
import com.umc.product.fcm.application.port.in.ManageFcmUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FcmController implements FcmControllerApi {

    private final ManageFcmUseCase manageFcmUseCase;

    @Override
    @PostMapping("/{memberId}")
    public void registerFcmToken(
            // TODO : 인증 적용 시 @PathVariable -> @AuthenticationPrincipal 변경 필요
            @PathVariable("memberId") Long userId,
            @RequestBody FcmRegistrationRequest request) {
        manageFcmUseCase.registerFcmToken(userId, request);
    }

    @Override
    @PostMapping("/test-send")
    public void sendTestNotification(@RequestBody FcmTestSendRequest request) {
        manageFcmUseCase.sendMessageByToken(request.toCommand());
    }
}
