package com.umc.product.report.adapter.out.delivery;

import org.springframework.stereotype.Component;

import com.umc.product.report.application.port.out.ReportDeliveryPort;
import com.umc.product.report.domain.Report;

/**
 * 운영 채널이 아직 연결되지 않았을 때의 전달 구현입니다.
 * <p>
 * 항상 비활성으로 응답하므로 신고는 전달 대기(PENDING)로 남습니다. 운영 채널(Discord 봇) 어댑터가 추가되면 이 구현을 대체합니다.
 */
@Component
public class DisabledReportDeliveryAdapter implements ReportDeliveryPort {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void deliver(Report report) {
        throw new IllegalStateException("신고 운영 채널이 연결되지 않았습니다.");
    }
}
