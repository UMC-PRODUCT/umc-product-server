package com.umc.product.notification.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notification.adapter.in.web.dto.response.NotificationHistoryItemResponse;
import com.umc.product.notification.application.port.in.query.ListMyNotificationsUseCase;
import com.umc.product.notification.application.port.in.query.dto.NotificationHistoryPageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification | 알림", description = "로그인한 회원의 알림 이력을 조회합니다.")
public class NotificationHistoryQueryController {

    private final ListMyNotificationsUseCase listMyNotificationsUseCase;

    @GetMapping
    @Operation(
        operationId = "NOTIFICATION-001",
        summary = "내 알림 목록 조회 (공지·리마인더)",
        description = """
            로그인한 회원의 알림을 커서 기반으로 최신순 조회합니다.
            현재 조회 범위는 공지 등록·리마인더 알림입니다.
            첫 요청에서는 cursor를 생략하고, 다음 페이지는 이전 응답의 nextCursor를 cursor로 전달합니다.
            """
    )
    public CursorResponse<NotificationHistoryItemResponse> listMyNotifications(
        @CurrentMember MemberPrincipal principal,
        @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 생략합니다.")
        @RequestParam(required = false) @Positive Long cursor,
        @Parameter(description = "페이지 크기. 기본 20, 최대 100입니다.")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        NotificationHistoryPageInfo page = listMyNotificationsUseCase.listByMemberId(
            principal.getMemberId(), cursor, size
        );
        return CursorResponse.of(
            page.content().stream().map(NotificationHistoryItemResponse::from).toList(),
            page.nextCursor(),
            page.hasNext()
        );
    }
}
