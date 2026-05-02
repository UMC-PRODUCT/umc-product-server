package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.PublishProjectCommand;
import com.umc.product.project.domain.enums.ProjectStatus;

/**
 * 프로젝트 공개 UseCase (PROJECT-108).
 * <p>
 * PENDING_REVIEW → IN_PROGRESS 전이 + 지원 폼 동반 publish (한 트랜잭션).
 */
public interface PublishProjectUseCase {

    ProjectStatus publish(PublishProjectCommand command);
}
