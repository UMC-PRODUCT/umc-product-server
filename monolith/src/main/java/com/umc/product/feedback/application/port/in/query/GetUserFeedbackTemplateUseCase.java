package com.umc.product.feedback.application.port.in.query;

import java.util.Optional;

import com.umc.product.feedback.application.port.in.query.dto.UserFeedbackTemplateInfo;
import com.umc.product.feedback.domain.enums.UserFeedbackContext;

/**
 * 사용자 피드백 템플릿 조회 UseCase.
 * <p>
 * 요청한 context 안에서 로그인 사용자에게 제공할 활성 템플릿과 폼 구조를 반환합니다.
 * 지원 설문은 현재 기수 챌린저에게, 매칭 설문은 역할·챌린저 이력과 관계없이 모든 로그인 사용자에게 제공합니다.
 * 운영진 지원 현황 설문은 기존처럼 운영진에게 제공합니다.
 * 지원 설문은 신규·기존 전용 템플릿을 우선 사용하고, 없으면 CHALLENGER 공통 템플릿을 사용합니다.
 * 매칭 설문은 ALL 템플릿을 사용합니다.
 * 운영진 지원 현황 설문은 같은 context의 ADMIN 템플릿을 조회합니다. 다른 context의 설문으로 대체하지 않습니다.
 */
public interface GetUserFeedbackTemplateUseCase {

    /**
     * 요청한 context의 템플릿을 선택한 뒤 해당 formId에 대한 사용자의 제출 이력을 확인합니다.
     * <p>
     * 활성 기수·설문 대상·선택 가능한 템플릿이 없거나, 선택한 폼에 이미 제출했다면 {@link Optional#empty()}를 반환합니다.
     *
     * @param requesterMemberId 사용자 유형 판별 및 제출 이력 확인에 사용할 로그인 사용자 ID
     * @param context           조회할 설문 종류
     * @return 요청한 context에서 선택한 미제출 템플릿과 폼 구조, 제공할 설문이 없으면 Optional.empty()
     */
    Optional<UserFeedbackTemplateInfo> findTemplate(Long requesterMemberId, UserFeedbackContext context);
}
