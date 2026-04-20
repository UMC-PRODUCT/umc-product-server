package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.command.dto.CreateOriginalWorkbookMissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.EditOriginalWorkbookMissionCommand;

/**
 * 원본 워크북 미션 관리 UseCase
 * <p>
 * 중앙운영사무국 교육국 소속 파트장 권한 필요
 */
public interface ManageOriginalWorkbookMissionUseCase {

    /**
     * 원본 워크북에 미션 추가
     * <p>
     * 이미 배포된 워크북에는 필수가 아닌 미션만 추가할 수 있습니다.
     *
     * @param command 생성 커맨드 (원본 워크북 ID, 제목, 설명, 유형, 필수 여부)
     * @return 생성된 미션 ID
     */
    Long createOriginalMission(CreateOriginalWorkbookMissionCommand command);

    /**
     * 원본 워크북 미션 수정
     * <p>
     * 워크북이 배포된 경우 필수→선택 방향으로만 변경 가능합니다.
     *
     * @param command 수정 커맨드 (미션 ID, 변경할 필드들)
     */
    void editOriginalMission(EditOriginalWorkbookMissionCommand command);

    /**
     * 원본 워크북 미션 삭제
     * <p>
     * 이미 제출한 챌린저가 있는 미션은 삭제할 수 없습니다.
     *
     * @param originalMissionId 삭제 대상 미션 ID
     */
    void deleteOriginalMission(Long originalMissionId);
}
