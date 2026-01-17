package com.umc.product.notification.adapter.in.web;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.adapter.in.web.dto.request.FcmTestSendRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "FCM", description = "FCM 푸시 알림 관리 API")
public interface FcmControllerApi {

    @Operation(
            summary = "FCM 토큰 등록",
            description = "사용자의 FCM 토큰을 등록하거나 업데이트합니다. 이미 등록된 토큰이 있으면 갱신됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER-0001: 사용자를 찾을 수 없습니다.")
    })
    void registerFcmToken(
            @Parameter(description = "사용자 ID", required = true) Long userId,
            FcmRegistrationRequest request
    );

    @Operation(
            summary = "푸시 알림 테스트 전송",
            description = "특정 사용자에게 테스트 푸시 알림을 전송합니다. 개발/테스트 용도로 사용됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 전송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = """
                    - MEMBER-0001: 사용자를 찾을 수 없습니다.
                    - FCM-0002: 해당 유저의 FCM 토큰을 찾을 수 없습니다.""")
    })
    void sendTestNotification(FcmTestSendRequest request);
}
