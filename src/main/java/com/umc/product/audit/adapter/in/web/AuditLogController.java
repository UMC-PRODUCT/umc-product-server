package com.umc.product.audit.adapter.in.web;

import com.umc.product.audit.application.port.in.query.GetAuditLogUseCase;
import com.umc.product.audit.application.port.in.query.dto.AuditLogInfo;
import com.umc.product.audit.application.port.in.query.dto.SearchAuditLogQuery;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit | 감사 로그 조회", description = "관리자용 감사 로그 조회 API")
public class AuditLogController {

    private final GetAuditLogUseCase getAuditLogUseCase;

    @Operation(summary = "감사 로그 검색")
    @CheckAccess(
        resourceType = ResourceType.AUDIT,
        permission = PermissionType.READ,
        message = "Audit Log는 중앙운영사무국 국원만 조회 가능합니다."
    )
    @GetMapping
    public ApiResponse<Page<AuditLogInfo>> search(
        @RequestParam(required = false) Domain domain,
        @RequestParam(required = false) AuditAction action,
        @RequestParam(required = false) Long actorMemberId,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @PageableDefault(size = 20) @ParameterObject Pageable pageable
    ) {
        SearchAuditLogQuery query = new SearchAuditLogQuery(domain, action, actorMemberId, from, to);
        Page<AuditLogInfo> result = getAuditLogUseCase.search(query, pageable);

        return ApiResponse.onSuccess(result);
    }
}
