package com.umc.product.notification.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.adapter.in.web.dto.request.FcmTestSendRequest;
import com.umc.product.notification.adapter.in.web.swagger.FcmControllerApi;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.in.RefreshFcmTokenUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification/fcm")
@RequiredArgsConstructor
public class FcmController implements FcmControllerApi {

    private final ManageFcmUseCase manageFcmUseCase;
    private final RefreshFcmTokenUseCase refreshFcmTokenUseCase;

    @Override
    @PutMapping("/token")
    public void refreshFcmToken(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody FcmRegistrationRequest request) {
        refreshFcmTokenUseCase.refreshTokenAndSubscriptions(memberPrincipal.getMemberId(), request);
    }

    @Override
    @PostMapping("/test-send")
    public void sendTestNotification(@RequestBody FcmTestSendRequest request) {
        manageFcmUseCase.sendMessageByToken(request.toCommand());
    }
}
