package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {
}
