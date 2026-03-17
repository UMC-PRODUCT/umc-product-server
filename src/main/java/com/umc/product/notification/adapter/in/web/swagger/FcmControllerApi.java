package com.umc.product.notification.adapter.in.web.swagger;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification | FCM", description = "")
public interface FcmControllerApi {

    @Operation(
        summary = "FCM 토큰 등록",
        description = "사용자의 FCM 토큰을 등록하거나 업데이트합니다. 이미 등록된 토큰이 있으면 갱신됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER-0001: 사용자를 찾을 수 없습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "FCM-0004: FCM 토픽 구독에 실패했습니다.")
    })
    void refreshFcmToken(
        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal,
        FcmRegistrationRequest request
    );

    @Operation(
        summary = "[마이그레이션] 기존 토픽 구독 해제",
        description = "prefix 없이 구독된 레거시 토픽을 일괄 해제합니다. 마이그레이션 일회성 용도입니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "레거시 토픽 구독 해제 성공")
    })
    void unsubscribeLegacyTopics(
        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );
}
