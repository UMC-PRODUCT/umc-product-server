package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.Submission;

public interface SaveSubmissionPort {

    Submission save(Submission submission);
}
