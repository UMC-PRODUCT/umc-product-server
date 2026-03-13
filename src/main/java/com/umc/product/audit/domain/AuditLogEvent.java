package com.umc.product.audit.domain;

import com.umc.product.global.exception.constant.Domain;
import java.util.Map;
import lombok.Builder;

/**
 * 감사 로그 이벤트
 * <p>
 * AOP Aspect 또는 서비스에서 직접 발행하여 비동기로 감사 로그를 저장합니다.
 */
@Builder
public record AuditLogEvent(
    Domain domain,
    AuditAction action,
    String targetType,
    String targetId,
    Long actorMemberId,
    String description,
    Map<String, Object> details,
    String ipAddress
) {
}
