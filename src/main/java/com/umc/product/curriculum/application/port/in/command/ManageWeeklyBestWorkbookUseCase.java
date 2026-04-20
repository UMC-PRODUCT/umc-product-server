package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.command.dto.CreateWeeklyBestWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.EditWeeklyBestWorkbookCommand;

/**
 * 베스트 워크북 선정 관리 UseCase
 * <p>
 * 스터디 그룹의 파트장 이상 권한 필요
 */
public interface ManageWeeklyBestWorkbookUseCase {

    /**
     * 베스트 워크북 선정
     * <p>
     * - 주차별로 스터디 그룹당 한 명만 지정 가능합니다.
     * - EXCUSED 처리된 워크북이 있는 챌린저는 선정 불가합니다.
     * - 해당 주차의 모든 필수 미션이 PASS 처리되어 있어야 합니다.
     *
     * @param command 선정 커맨드 (선정자 ID, 대상 멤버 ID, 주차별 커리큘럼 ID, 스터디 그룹 ID, 사유)
     */
    void createWeeklyBestWorkbook(CreateWeeklyBestWorkbookCommand command);

    /**
     * 베스트 워크북 선정 사유 수정
     *
     * @param command 수정 커맨드 (WeeklyBestWorkbook PK, 요청자 멤버 ID, 변경할 사유)
     */
    void editWeeklyBestWorkbookReason(EditWeeklyBestWorkbookCommand command);

    /**
     * 베스트 워크북 선정 철회
     * <p>
     * 해당 주차 종료 후 1주일 이내에만 철회 가능합니다.
     *
     * @param weeklyBestWorkbookId WeeklyBestWorkbook Entity PK
     */
    void deleteWeeklyBestWorkbook(Long weeklyBestWorkbookId);
}