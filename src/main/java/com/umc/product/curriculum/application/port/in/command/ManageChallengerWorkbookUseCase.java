package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.command.dto.CancelBestWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.ReviewWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SelectBestWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SubmitChallengerWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SubmitWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.UpdateBestReasonCommand;
import com.umc.product.curriculum.application.port.in.command.dto.UpdateReviewCommand;

/**
 * 워크북 관리 UseCase
 * <p>
 * 챌린저의 워크북 제출 및 운영진의 워크북 검토/베스트 선정 기능을 제공합니다.
 */
public interface ManageChallengerWorkbookUseCase {


    // <------------------ 챌린저 전용 -------------------------->
    /**
     * 워크북 제출 (챌린저 전용)
     * <p>
     * 챌린저가 워크북에 링크(깃허브, 노션 등)를 제출합니다.
     * 제출 시 워크북 상태가 PENDING → SUBMITTED로 변경됩니다.
     *
     * @param command 제출 커맨드 (워크북 ID, 제출 링크)
     * @deprecated 1.3.0부터 {@link #(SubmitChallengerWorkbookCommand)} 사용
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    void submit(SubmitWorkbookCommand command);

    /**
     * 챌린저 워크북 ID 기반 워크북 제출 (챌린저 전용)
     * <p>
     * 이미 배포된 챌린저 워크북에 링크(깃허브, 노션 등)를 제출합니다.
     * 제출 시 워크북 상태가 PENDING → SUBMITTED로 변경됩니다.
     *
     * @param command 제출 커맨드 (챌린저 워크북 ID, 제출 링크)
     */
    void submitByChallengerWorkbookId(SubmitChallengerWorkbookCommand command);


    // <---------------------- 운영진 전용 -------------------------->
    /**
     * 챌린저 워크북 검토 (운영진 전용)
     * <p>
     * 운영진이 제출된 챌린저 워크북을 검토하여 통과(PASS) 또는 반려(FAIL) 처리합니다.
     *
     * @param command 검토 커맨드 (워크북 ID, 검토 결과, 피드백)
     */
    void review(ReviewWorkbookCommand command);


    /**
     * 베스트 워크북 취소 (운영진 전용)
     * <p>
     * 베스트 선정을 취소하고 워크북을 PASS 상태로 되돌립니다.
     * 연결된 모든 BEST 리뷰가 PASS로 변경되고 bestReason이 삭제됩니다.
     *
     * @param command 베스트 취소 커맨드 (워크북 ID)
     */
    void cancelBest(CancelBestWorkbookCommand command);


    /**
     * 베스트 워크북 선정 (운영진 전용)
     * <p>
     * 운영진이 우수한 워크북을 베스트로 선정합니다.
     * 작성한 기존 PASS 리뷰가 있어야 선정 가능합니다.
     *
     * @param command 베스트 선정 커맨드 (워크북 ID, 추천사)
     */
    void selectBest(SelectBestWorkbookCommand command);

    /**
     * 베스트 추천사 수정 (운영진 전용)
     * <p>
     * 리뷰 ID를 통해 베스트 추천사를 수정합니다.
     *
     * @param command 추천사 수정 커맨드 (리뷰 ID, 추천사)
     */
    void updateBestReason(UpdateBestReasonCommand command);

    /**
     * 리뷰 수정 (운영진 전용)
     * <p>
     * 리뷰의 심사 결과(PASS/FAIL)와 피드백을 수정합니다.
     * BEST 상태의 리뷰는 수정 불가 — cancelBest 후 수정해야 합니다.
     * 수정 후 워크북 상태가 자동으로 재계산됩니다.
     *
     * @param command 리뷰 수정 커맨드 (리뷰 ID, 심사 결과, 피드백)
     */
    void updateReview(UpdateReviewCommand command);
}
