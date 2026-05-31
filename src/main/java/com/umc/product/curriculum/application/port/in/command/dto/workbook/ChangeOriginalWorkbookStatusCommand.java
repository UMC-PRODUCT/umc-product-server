package com.umc.product.curriculum.application.port.in.command.dto.workbook;

import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 원본 워크북 상태 변경 커맨드
 *
 * <p>허용된 상태 전환:
 * <ul>
 *   <li>DRAFT → READY (배포 준비 등록)</li>
 *   <li>READY → RELEASED (수동/자동 배포)</li>
 *   <li>READY → DRAFT (임시저장으로 롤백)</li>
 *   <li>RELEASED → any: 불가 (배포 완료 후 되돌리기 불가)</li>
 * </ul>
 *
 * @param originalWorkbookId 상태를 변경할 원본 워크북 ID
 * @param status             변경할 상태 (DRAFT, READY, RELEASED 중 허용된 전환만 가능)
 * @param requestedMemberId  요청 운영진의 멤버 ID (RELEASED 전환 시 releasedMemberId로 기록)
 */
@Builder
public record ChangeOriginalWorkbookStatusCommand(
    @NotNull(message = "원본 워크북 ID는 필수입니다")
    Long originalWorkbookId,

    @NotNull(message = "변경할 상태는 필수입니다")
    OriginalWorkbookStatus status,

    @NotNull(message = "요청자 멤버 ID는 필수입니다")
    Long requestedMemberId
) {
}
