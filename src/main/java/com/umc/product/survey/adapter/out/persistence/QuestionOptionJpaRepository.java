package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionOptionJpaRepository extends JpaRepository<QuestionOption, Long> {
}
