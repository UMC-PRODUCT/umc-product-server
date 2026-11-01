package com.umc.product.project.adapter.in.web.dto.request;

import java.time.Instant;
import java.util.List;

import com.umc.product.project.application.port.in.command.dto.CreateProjectMatchingRoundCommand;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveProjectMatchingRoundsRequest(
    @NotNull(message = "매칭 유형은 필수입니다") MatchingType type,
    @NotNull(message = "차수 목록은 필수입니다") @Size(
        min = 4, max = 4,
        message = "1~3차와 랜덤 매칭을 모두 입력해주세요"
    )
    List<@NotNull @Valid RoundRequest> rounds
) {
    public List<CreateProjectMatchingRoundCommand> toCommands(Long requesterMemberId, Long chapterId) {
        return rounds.stream().map(round -> CreateProjectMatchingRoundCommand.builder()
            .requesterMemberId(requesterMemberId)
            .chapterId(chapterId)
            .type(type)
            .name(round.name())
            .description(round.description())
            .phase(round.phase())
            .startsAt(round.startsAt())
            .endsAt(round.endsAt())
            .decisionDeadline(round.phase() == MatchingPhase.RANDOM_MATCHING
                ? round.endsAt() : round.decisionDeadline())
            .resultPublishAt(round.resultPublishAt())
            .build()).toList();
    }

    public record RoundRequest(
        @NotBlank(message = "매칭 차수 이름은 필수입니다") @Size(
            max = 255,
            message = "매칭 차수 이름은 255자 이하여야 합니다"
        ) String name,
        @Size(max = 255, message = "매칭 차수 설명은 255자 이하여야 합니다") String description,
        @NotNull(message = "매칭 차수 단계는 필수입니다") MatchingPhase phase,
        @NotNull(message = "매칭 시작 시각은 필수입니다") Instant startsAt,
        @NotNull(message = "매칭 종료 시각은 필수입니다") Instant endsAt,
        Instant decisionDeadline,
        @NotNull(message = "결과 발표 시각은 필수입니다") Instant resultPublishAt
    ) {
        @AssertTrue(
            message = "1~3차는 결정 마감이 필수이며 랜덤 매칭은 결정 마감을 입력하지 않습니다"
        )
        @Schema(
            hidden = true
        )
        public boolean isDecisionDeadlineValid() {
            return phase == MatchingPhase.RANDOM_MATCHING ? decisionDeadline == null : decisionDeadline != null;
        }
    }
}
