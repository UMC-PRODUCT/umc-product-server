package com.umc.product.curriculum.application.port.in.command.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;

/**
 * 주차별 커리큘럼 생성 커맨드
 *
 * @param curriculumId 상위 커리큘럼 ID
 * @param weekNo       주차 번호
 * @param isExtra      부록 여부 (false = 정규, true = 부록)
 * @param title        주차 제목
 * @param startsAt     주차 시작 일시
 * @param endsAt       주차 종료 일시
 */
@Builder
public record CreateWeeklyCurriculumCommand(
    @NotNull(message = "커리큘럼 ID는 필수입니다")
    Long curriculumId,

    @NotNull(message = "주차 번호는 필수입니다")
    Long weekNo,

    @NotNull(message = "부록 여부는 필수입니다.")
    Boolean isExtra,

    @NotBlank(message = "제목은 필수입니다")
    String title,

    @NotNull(message = "시작 일시는 필수입니다")
    Instant startsAt,

    @NotNull(message = "종료 일시는 필수입니다")
    Instant endsAt
) {
}
