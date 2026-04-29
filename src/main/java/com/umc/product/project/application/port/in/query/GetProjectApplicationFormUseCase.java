package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import java.util.Optional;

/**
 * 프로젝트 지원 폼 조회 UseCase (PROJECT-106-GET).
 * <p>
 * 폼이 없으면 {@link Optional#empty()} 를 반환하며, Controller 단에서 {@code ApiResponse.result = null} 로 매핑된다.
 * 챌린저 호출 시 본인 파트 + 공통 섹션만 노출하는 마스킹 로직은 PR3b 에서 도입 예정이며,
 * 본 UseCase 는 raw 정보를 그대로 반환한다.
 */
public interface GetProjectApplicationFormUseCase {

    /**
     * 특정 프로젝트의 지원 폼 전체 구조를 조회합니다.
     *
     * @return 폼이 있으면 {@link ApplicationFormInfo}, 없으면 {@link Optional#empty()}
     */
    Optional<ApplicationFormInfo> findByProjectId(Long projectId);
}
