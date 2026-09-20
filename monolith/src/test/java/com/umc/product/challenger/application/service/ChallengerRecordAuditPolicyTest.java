package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.global.exception.constant.Domain;

class ChallengerRecordAuditPolicyTest {

    @Test
    @DisplayName("챌린저 기록 생성은 감사 로그 대상으로 선언한다")
    void create_is_audited() throws NoSuchMethodException {
        Audited audited = audited("create", CreateChallengerRecordCommand.class);

        assertThat(audited).isNotNull();
        assertThat(audited.domain()).isEqualTo(Domain.CHALLENGER);
        assertThat(audited.action()).isEqualTo(AuditAction.CREATE);
        assertThat(audited.targetType()).isEqualTo("ChallengerRecord");
        assertThat(audited.targetId()).isEqualTo("#result");
    }

    @Test
    @DisplayName("챌린저 기록 대량 생성은 감사 로그 대상으로 선언한다")
    void create_bulk_is_audited() throws NoSuchMethodException {
        Audited audited = audited("createBulk", List.class);

        assertThat(audited).isNotNull();
        assertThat(audited.domain()).isEqualTo(Domain.CHALLENGER);
        assertThat(audited.action()).isEqualTo(AuditAction.CREATE);
        assertThat(audited.targetType()).isEqualTo("ChallengerRecord");
        assertThat(audited.targetId()).isEmpty();
    }

    @Test
    @DisplayName("챌린저 기록 삭제는 감사 로그 대상으로 선언한다")
    void delete_is_audited() throws NoSuchMethodException {
        Audited audited = audited("delete", Long.class);

        assertThat(audited).isNotNull();
        assertThat(audited.domain()).isEqualTo(Domain.CHALLENGER);
        assertThat(audited.action()).isEqualTo(AuditAction.DELETE);
        assertThat(audited.targetType()).isEqualTo("ChallengerRecord");
        assertThat(audited.targetId()).isEqualTo("#id");
    }

    private Audited audited(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = ChallengerRecordCommandService.class.getMethod(methodName, parameterTypes);
        return method.getAnnotation(Audited.class);
    }
}
