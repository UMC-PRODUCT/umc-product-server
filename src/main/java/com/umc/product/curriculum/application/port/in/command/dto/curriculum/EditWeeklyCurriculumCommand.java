package com.umc.product.curriculum.application.port.in.command.dto.curriculum;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;

/**
 * 주차별 커리큘럼 수정 커맨드
 *
 * @param weeklyCurriculumId 수정 대상 주차별 커리큘럼 ID
 * @param weekNo             변경할 주차 번호 (nullable: 미제공 시 유지)
 * @param isExtra            부록 여부 (nullable: 미제공 시 유지)
 * @param title              변경할 제목 (nullable: 미제공 시 유지)
 * @param startsAt           변경할 시작 일시 (nullable: 미제공 시 유지)
 * @param endsAt             변경할 종료 일시 (nullable: 미제공 시 유지)
 */
@Builder
public record EditWeeklyCurriculumCommand(
    @NotNull(message = "주차별 커리큘럼 ID는 필수입니다")
    Long weeklyCurriculumId,

    Long weekNo,
    Boolean isExtra,
    String title,
    Instant startsAt,
    Instant endsAt
) {
}
