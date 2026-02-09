package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewLiveQuestionRepository extends JpaRepository<InterviewLiveQuestion, Long> {

    int countByApplicationId(Long applicationId);

    List<InterviewLiveQuestion> findByApplicationIdOrderByIdAsc(Long applicationId);
}
