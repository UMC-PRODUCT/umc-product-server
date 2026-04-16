package com.umc.product.schedule.adapter.in.web.v2.dto.request;

/**
 * 각 출석 요청에 대해서 승인 및 거절 여부를 결정합니다.
 *
 * @param participantMemberId 어떤 사용자의 출석 요청에 대한 것인지를 나타냅니다.
 * @param isApproved          승인 여부입니다. true인 경우 승인, false인 경우 거절로 간주됩니다.
 * @param reason              승인 또는 거절 사유가 있다면 제공합니다. nullable!
 */
public record DecideAttendanceRequest(
    Long participantMemberId,
    boolean isApproved,
    String reason
) {
}
