package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.UserFeedbackTemplate;
import com.umc.product.project.domain.enums.UserFeedbackContext;
import com.umc.product.project.domain.enums.UserFeedbackTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFeedbackTemplateJpaRepository extends JpaRepository<UserFeedbackTemplate, Long> {

    Optional<UserFeedbackTemplate> findByContextAndTargetTypeAndIsActiveTrue(UserFeedbackContext context, UserFeedbackTargetType targetType);
}
