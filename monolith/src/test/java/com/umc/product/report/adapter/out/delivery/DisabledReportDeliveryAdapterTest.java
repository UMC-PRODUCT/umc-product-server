package com.umc.product.report.adapter.out.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportTargetType;

@DisplayName("운영 채널 미연결 전달 어댑터")
class DisabledReportDeliveryAdapterTest {

    private final DisabledReportDeliveryAdapter sut = new DisabledReportDeliveryAdapter();

    @Test
    @DisplayName("항상 비활성으로 응답하고, 보내려 하면 예외를 던진다")
    void 항상_비활성이다() {
        assertThat(sut.isEnabled()).isFalse();
        assertThatThrownBy(() -> sut.deliver(Report.create(1L, ReportTargetType.POST, 2L, null)))
            .isInstanceOf(IllegalStateException.class);
    }
}
