package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.ReviewWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SelectBestWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SubmitChallengerWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.SubmitWorkbookCommand;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadSubmissionPort;
import com.umc.product.curriculum.application.port.out.SaveChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadReviewPort;
import com.umc.product.curriculum.application.port.out.SaveReviewPort;
import com.umc.product.curriculum.application.port.out.SaveSubmissionPort;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.Review;
import com.umc.product.curriculum.domain.Submission;
import com.umc.product.curriculum.domain.enums.ReviewResult;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengerWorkbookCommandService implements ManageChallengerWorkbookUseCase {

    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveChallengerWorkbookPort saveChallengerWorkbookPort;
    private final SaveSubmissionPort saveSubmissionPort;
    private final LoadSubmissionPort loadSubmissionPort;
    private final SaveReviewPort saveReviewPort;
    private final LoadReviewPort loadReviewPort;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    public void submit(SubmitWorkbookCommand command) {
        OriginalWorkbook originalWorkbook = loadOriginalWorkbookPort.findById(command.originalWorkbookId());

        // 이미 제출한 이력이 있는지 확인
        if (!loadChallengerWorkbookPort.findAllByChallengerIdAndOriginalWorkbookId(
            command.challengerId(),
            originalWorkbook.getId()
        ).isEmpty()) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_SUBMISSION_ALREADY_EXISTS);
        }

        ChallengerWorkbook challengerWorkbook = ChallengerWorkbook.create(
            command.challengerId(),
            originalWorkbook.getId(),
            WorkbookStatus.PENDING,
            null
        );

        challengerWorkbook.submit();
        saveChallengerWorkbookPort.save(challengerWorkbook);

        Submission submission = Submission.create(challengerWorkbook.getId(), command.submission());
        saveSubmissionPort.save(submission);
    }

    @Override
    public void submitByWorkbookId(SubmitChallengerWorkbookCommand command) {
        ChallengerWorkbook challengerWorkbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());

        // 본인 워크북인지 확인 (challengerId → memberId 비교)
        verifyWorkbookOwner(challengerWorkbook, command.memberId());

        challengerWorkbook.submit();
        saveChallengerWorkbookPort.save(challengerWorkbook);

        Submission submission = Submission.create(challengerWorkbook.getId(), command.submission());
        saveSubmissionPort.save(submission);
    }

    @Override
    public void review(ReviewWorkbookCommand command) {
        ChallengerWorkbook challengerWorkbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());

        Submission submission = loadSubmissionPort.findByChallengerWorkbookId(command.challengerWorkbookId())
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.SUBMISSION_NOT_FOUND));

        Long reviewerChallengerId = resolveReviewerChallengerId(command.memberId(), challengerWorkbook.getChallengerId());
        validateNotAlreadyReviewed(submission.getId(), reviewerChallengerId);

        challengerWorkbook.review(command.status());

        Review review = Review.create(submission.getId(), reviewerChallengerId, ReviewResult.valueOf(command.status().name()), command.feedback());
        saveReviewPort.save(review);
    }


    private void validateNotAlreadyReviewed(Long submissionId, Long reviewerChallengerId) {
        if (loadReviewPort.existsBySubmissionIdAndReviewerChallengerId(submissionId, reviewerChallengerId)) {
            throw new CurriculumDomainException(CurriculumErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    /**
     * memberId로부터 리뷰어의 challengerId를 조회합니다.
     * 워크북 소유자의 gisuId를 기준으로 같은 기수의 챌린저를 조회합니다.
     */
    private Long resolveReviewerChallengerId(Long memberId, Long workbookChallengerId) {
        ChallengerInfo workbookOwner = getChallengerUseCase.getChallengerPublicInfo(workbookChallengerId);
        ChallengerInfo reviewer = getChallengerUseCase.getByMemberIdAndGisuId(memberId, workbookOwner.gisuId());
        return reviewer.challengerId();
    }

    @Override
    public void selectBest(SelectBestWorkbookCommand command) {
        // TODO: 베스트 워크북 선정 비즈니스 로직 구현
        // 1. 워크북 소유자의 스터디 그룹에서 리더(파트장)를 찾기
        // 2. 리더의 challengerId를 리뷰어로 사용
        // 3. 해당 리뷰어의 기존 PASS 리뷰가 존재해야 베스트 선정 가능 (REVIEW_NOT_FOUND)
        // 4. 스터디 그룹당 베스트 1명 제약조건 확인
        // 5. workbook.selectBest() → 상태 전이 (PASS → BEST)
        // 6. review.upgradeToBest(bestReason) → 리뷰 상태 변경
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void cancelBest(CancelBestWorkbookCommand command) {
        ChallengerWorkbook workbook = loadChallengerWorkbookPort.findById(command.challengerWorkbookId());

        workbook.cancelBest();

        Submission submission = loadSubmissionPort.getByChallengerWorkbookId(command.challengerWorkbookId());
        List<Review> bestReviews = loadReviewPort.findAllBySubmissionIdAndStatus(submission.getId(), ReviewResult.BEST);
        bestReviews.forEach(Review::cancelBest);
    }

        Review review = Review.createBest(submission.getId(), reviewerChallengerId, null, command.bestReason());
        saveReviewPort.save(review);
    }

    private void verifyWorkbookOwner(ChallengerWorkbook workbook, Long memberId) {
        ChallengerInfo challenger = getChallengerUseCase.getById(workbook.getChallengerId());
        if (!challenger.memberId().equals(memberId)) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_ACCESS_DENIED);
        }
    }
}
