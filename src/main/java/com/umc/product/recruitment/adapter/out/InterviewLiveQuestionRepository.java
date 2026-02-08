package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewLiveQuestionRepository extends JpaRepository<InterviewLiveQuestion, Long> {

    int countByApplicationId(Long applicationId);
}
