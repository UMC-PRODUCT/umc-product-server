package com.umc.product.audit.application.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.audit.application.port.in.command.SaveAuditLogUseCase;
import com.umc.product.audit.application.port.out.SaveAuditLogPort;
import com.umc.product.audit.domain.AuditLog;
import com.umc.product.audit.domain.AuditLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogCommandService implements SaveAuditLogUseCase {

    private final SaveAuditLogPort saveAuditLogPort;
    private final ObjectMapper objectMapper;

    @Override
    public void save(AuditLogEvent event) {
        String detailsJson = serializeDetails(event);

        AuditLog auditLog = AuditLog.from(event, detailsJson, event.ipAddress());
        saveAuditLogPort.save(auditLog);

        log.debug("[AUDIT] domain={}, action={}, target={}:{}, actor={}",
            event.domain(), event.action(), event.targetType(), event.targetId(), event.actorMemberId());
    }

    private String serializeDetails(AuditLogEvent event) {
        if (event.details() == null || event.details().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(event.details());
        } catch (JsonProcessingException e) {
            log.warn("감사 로그 details 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
}
