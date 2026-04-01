package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.application.port.out.LoadSubmissionPort;
import com.umc.product.curriculum.application.port.out.SaveSubmissionPort;
import com.umc.product.curriculum.domain.Submission;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionPersistenceAdapter implements LoadSubmissionPort, SaveSubmissionPort {

    private final SubmissionJpaRepository submissionJpaRepository;

    @Override
    public Optional<Submission> findByChallengerWorkbookId(Long challengerWorkbookId) {
        return submissionJpaRepository.findByChallengerWorkbookId(challengerWorkbookId);
    }

    @Override
    public Submission getByChallengerWorkbookId(Long challengerWorkbookId) {
        return submissionJpaRepository.findByChallengerWorkbookId(challengerWorkbookId)
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.SUBMISSION_NOT_FOUND));
    }

    @Override
    public Submission save(Submission submission) {
        return submissionJpaRepository.save(submission);
    }
}
