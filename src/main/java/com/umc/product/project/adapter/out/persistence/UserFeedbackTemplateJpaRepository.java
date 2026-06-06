package com.umc.product.project.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.project.domain.UserFeedbackTemplate;
import com.umc.product.project.domain.enums.UserFeedbackContext;
import com.umc.product.project.domain.enums.UserFeedbackTargetType;

public interface UserFeedbackTemplateJpaRepository extends JpaRepository<UserFeedbackTemplate, Long> {

    Optional<UserFeedbackTemplate> findByContextAndTargetTypeAndIsActiveTrue(UserFeedbackContext context, UserFeedbackTargetType targetType);
}
