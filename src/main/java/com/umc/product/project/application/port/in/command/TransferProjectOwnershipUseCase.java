package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.TransferProjectOwnershipCommand;
import com.umc.product.project.domain.enums.ProjectStatus;

/**
 * 프로젝트 소유권(메인 PM) 양도 UseCase.
 * <p>
 * 양도는 권한 이동(EDIT/SUBMIT 권한)을 동반하는 의도된 액션이므로 일반 PATCH가 아닌
 * 별도 엔드포인트로 분리합니다.
 */
public interface TransferProjectOwnershipUseCase {

    /**
     * 프로젝트 소유권을 새 PM에게 양도합니다.
     *
     * @return 양도 후 프로젝트 상태
     */
    ProjectStatus transfer(TransferProjectOwnershipCommand command);
}
