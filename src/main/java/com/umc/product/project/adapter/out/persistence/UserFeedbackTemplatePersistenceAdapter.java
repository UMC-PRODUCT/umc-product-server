package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadUserFeedbackTemplatePort;
import com.umc.product.project.domain.UserFeedbackTemplate;
import com.umc.product.project.domain.enums.UserFeedbackContext;
import com.umc.product.project.domain.enums.UserFeedbackTargetType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFeedbackTemplatePersistenceAdapter implements LoadUserFeedbackTemplatePort {

    private final UserFeedbackTemplateJpaRepository repository;

    @Override
    public Optional<UserFeedbackTemplate> findByContextAndTargetType(UserFeedbackContext context, UserFeedbackTargetType targetType) {
        return repository.findByContextAndTargetTypeAndIsActiveTrue(context, targetType);
    }

    @Override
    public UserFeedbackTemplate getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.USER_FEEDBACK_TEMPLATE_NOT_FOUND));
    }
}
