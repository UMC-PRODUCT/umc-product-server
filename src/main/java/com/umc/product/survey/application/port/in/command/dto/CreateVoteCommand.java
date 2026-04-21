package com.umc.product.survey.application.port.in.command.dto;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteCommand;
import lombok.Builder;

import java.util.List;

/**
 * 투표용 설문 생성 명령 DTO
 */
@Builder
public record CreateVoteCommand(
    Long createdMemberId,
    String title,
    boolean isAnonymous,
    boolean allowMultipleChoice,
    List<String> options
) {
    public static CreateVoteCommand from(AddNoticeVoteCommand command) {
        return CreateVoteCommand.builder()
            .createdMemberId(command.createdMemberId())
            .title(command.title())
            .isAnonymous(command.isAnonymous())
            .allowMultipleChoice(command.allowMultipleChoice())
            .options(command.options())
            .build();
    }
}
