package com.umc.product.audit.domain;

import com.umc.product.global.exception.constant.Domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 감사 로그 엔티티 (immutable, BaseEntity 미상속)
 */
@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Domain domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(nullable = false, length = 100)
    private String targetType;

    @Column(length = 255)
    private String targetId;

    private Long actorMemberId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String details;

    @Column(length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private AuditLog(
        Domain domain, AuditAction action, String targetType, String targetId,
        Long actorMemberId, String description, String details, String ipAddress
    ) {
        this.domain = domain;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.actorMemberId = actorMemberId;
        this.description = description;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public static AuditLog from(AuditLogEvent event, String detailsJson, String ipAddress) {
        return AuditLog.builder()
            .domain(event.domain())
            .action(event.action())
            .targetType(event.targetType())
            .targetId(event.targetId())
            .actorMemberId(event.actorMemberId())
            .description(event.description())
            .details(detailsJson)
            .ipAddress(ipAddress)
            .build();
    }
}
