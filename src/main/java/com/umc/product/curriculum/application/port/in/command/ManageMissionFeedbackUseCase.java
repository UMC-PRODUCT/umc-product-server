package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.DeleteMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionFeedbackCommand;

/**
 * 미션 피드백 관리 UseCase (운영진 전용)
 */
public interface ManageMissionFeedbackUseCase {

    /**
     * 제출된 미션에 대한 피드백 작성
     * <p>
     * - 스터디 일정이 등록된 경우 일정 당일 00:00 이후에만 피드백 작성이 가능합니다.
     * - N주차 워크북에 대한 피드백은 N+1주차 수요일 00:00 이전까지 제공되어야 합니다.
     * - 선택 미션에 대한 피드백은 기간이 경과되어도 불이익이 없습니다.
     *
     * @param command 작성 커맨드 (미션 제출물 ID, 작성자 멤버 ID, 내용, 평가 결과)
     * @return 생성된 피드백 ID
     */
    Long createFeedback(CreateMissionFeedbackCommand command);

    /**
     * 제출된 미션 피드백 수정
     * <p>
     * - 작성일 기준 2주가 경과되기 전까지만 수정 가능합니다.
     * - PASS → FAIL 변경은 불가능합니다.
     *
     * @param command 수정 커맨드 (피드백 ID, 요청자 멤버 ID, 변경할 내용)
     */
    void editFeedback(EditMissionFeedbackCommand command);

    /**
     * 제출된 미션 피드백 삭제
     * <p>
     * 해당 기수 종료 이후에는 삭제 불가능합니다.
     *
     * @param command 삭제 커맨드 (피드백 ID, 요청자 멤버 ID)
     */
    void deleteFeedback(DeleteMissionFeedbackCommand command);
}
