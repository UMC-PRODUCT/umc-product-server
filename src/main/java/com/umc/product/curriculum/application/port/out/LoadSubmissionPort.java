package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.Submission;
import java.util.Optional;

public interface LoadSubmissionPort {

    Optional<Submission> findByChallengerWorkbookId(Long challengerWorkbookId);

    Submission getByChallengerWorkbookId(Long challengerWorkbookId);

    Submission getById(Long submissionId);
}
