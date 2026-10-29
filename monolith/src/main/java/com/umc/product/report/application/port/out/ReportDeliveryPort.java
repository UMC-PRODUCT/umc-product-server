package com.umc.product.report.application.port.out;

import com.umc.product.report.domain.Report;

/**
 * 신고를 운영 채널로 보내는 포트입니다.
 */
public interface ReportDeliveryPort {

    /**
     * 운영 채널이 설정되어 있어 실제로 보낼 수 있는지 확인합니다.
     */
    boolean isEnabled();

    /**
     * 신고를 운영 채널로 보냅니다. 전달에 실패하면 예외를 던집니다.
     */
    void deliver(Report report);
}
