package com.umc.product.curriculum.application.port.in.command;

/**
 * 워크북 관리 UseCase
 * <p>
 * 챌린저의 워크북 제출 및 운영진의 워크북 검토/베스트 선정 기능을 제공합니다.
 */
public interface ManageWorkbookUseCase {

    /**
     * 워크북 제출 (챌린저 전용)
     * <p>
     * 챌린저가 워크북에 링크(깃허브, 노션 등)를 제출합니다.
     * 제출 시 워크북 상태가 PENDING → SUBMITTED로 변경됩니다.
     *
     * @param command 제출 커맨드 (워크북 ID, 제출 링크)
     */
    void submit(SubmitWorkbookCommand command);

    /**
     * 워크북 검토 (운영진 전용)
     * <p>
     * 운영진이 제출된 워크북을 검토하여 통과(PASS) 또는 반려(FAIL) 처리합니다.
     *
     * @param command 검토 커맨드 (워크북 ID, 검토 결과, 피드백)
     */
    void review(ReviewWorkbookCommand command);

    /**
     * 베스트 워크북 선정 (운영진 전용)
     * <p>
     * 운영진이 우수한 워크북을 베스트로 선정합니다.
     *
     * @param command 베스트 선정 커맨드 (워크북 ID, 추천사)
     */
    void selectBest(SelectBestWorkbookCommand command);
}
