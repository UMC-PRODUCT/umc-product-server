package com.umc.product.inquiry.application.port.in.command.dto;

/**
 * @param labelId 새로 만들 때는 null.
 */
public record SaveInquiryLabelCommand(
    Long actorMemberId,
    Long labelId,
    String name
) {
}
