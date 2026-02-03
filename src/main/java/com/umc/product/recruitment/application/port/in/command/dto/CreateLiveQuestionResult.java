package com.umc.product.recruitment.application.port.in.command.dto;

public record CreateLiveQuestionResult(
        Long liveQuestionId,
        Integer orderNo,
        String text,
        CreatedBy createdBy,
        Boolean canEdit
) {
    public record CreatedBy(Long memberId, String nickname, String name) {
    }
}