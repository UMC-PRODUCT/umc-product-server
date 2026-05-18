package com.umc.product.notification.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.adapter.in.web.swagger.FcmControllerApi;
import com.umc.product.notification.application.port.in.ManageFcmTopicUseCase;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final ManageFcmTopicUseCase manageFcmTopicUseCase;

    @Override
    @PutMapping("/token")
    public void refreshFcmToken(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody FcmRegistrationRequest request) {
        manageFcmUseCase.registerFcmToken(memberPrincipal.getMemberId(), request);
    }

    @Override
    @DeleteMapping("/topics/legacy")
    public void unsubscribeAllMemberLegacyTopics(@CurrentMember MemberPrincipal memberPrincipal) {
        manageFcmTopicUseCase.unsubscribeLegacyTopics(memberPrincipal.getMemberId());
    }

    @Override
    public void resubscribeAllMemberLegacyTopics() {
        manageFcmTopicUseCase.resubscribeAllLegacyTopics();
    }


}
