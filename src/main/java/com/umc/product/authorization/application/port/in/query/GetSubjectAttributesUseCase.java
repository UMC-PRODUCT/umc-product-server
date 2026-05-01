package com.umc.product.authorization.application.port.in.query;

import com.umc.product.authorization.domain.SubjectAttributes;

/**
 * memberId 로부터 권한 평가에 필요한 {@link SubjectAttributes}(역할/소속 기수/학교 등)를 조립한다.
 * <p>
 * Controller 가 Service 에 권한/scope 정보를 넘겨야 하는 경우 본 UseCase 를 주입해 사용한다.
 */
public interface GetSubjectAttributesUseCase {

    SubjectAttributes getByMemberId(Long memberId);
}
