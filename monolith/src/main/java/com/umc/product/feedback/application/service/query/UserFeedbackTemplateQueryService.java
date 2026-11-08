package com.umc.product.feedback.application.service.query;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.feedback.application.port.in.query.GetUserFeedbackTemplateUseCase;
import com.umc.product.feedback.application.port.in.query.dto.UserFeedbackTemplateInfo;
import com.umc.product.feedback.application.port.out.LoadUserFeedbackTemplatePort;
import com.umc.product.feedback.domain.UserFeedbackTemplate;
import com.umc.product.feedback.domain.enums.UserFeedbackContext;
import com.umc.product.feedback.domain.enums.UserFeedbackTargetType;
import com.umc.product.form.application.port.in.query.GetFormResponseUseCase;
import com.umc.product.form.application.port.in.query.GetFormUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 피드백 템플릿 조회 서비스.
 * <p>
 * 지원 설문은 현재 기수 챌린저 이력을 기준으로 대상을 판별합니다.
 * 매칭 설문은 역할·챌린저 이력과 관계없이 모든 로그인 사용자에게 ALL 템플릿을 제공합니다.
 * 운영진 지원 현황 설문은 현재 기수 중앙 운영진 또는 SUPER_ADMIN에게 제공합니다.
 * 지원 설문은 신규·기존 챌린저 전용 템플릿이 없으면 같은 context의 CHALLENGER 공통 템플릿을 선택합니다.
 * 운영진 설문은 ADMIN 템플릿만 선택하며, 다른 context의 설문을 대신 반환하지 않습니다.
 * 선택한 formId에 사용자가 이미 제출했다면 빈 결과를 반환하고, 미제출이면 템플릿과 폼 구조를 반환합니다.
 * <p>
 * <b>10기 한정 분류 예외:</b> 기획-디자인 매칭 경험을 반영하여 10기 신규 PM은 EXPERIENCED_CHALLENGER로 분류합니다.
 * 11기를 포함한 다른 기수에는 이 예외를 적용하지 않습니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFeedbackTemplateQueryService implements GetUserFeedbackTemplateUseCase {

    private final LoadUserFeedbackTemplatePort loadUserFeedbackTemplatePort;

    // Cross-domain
    private final GetGisuUseCase getGisuUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetFormUseCase getFormUseCase;
    private final GetFormResponseUseCase getFormResponseUseCase;

    @Override
    public Optional<UserFeedbackTemplateInfo> findTemplate(Long requesterMemberId, UserFeedbackContext context) {
        return getGisuUseCase.findActiveGisu()
            .flatMap(gisu -> resolveTargetType(requesterMemberId, gisu.gisuId(), gisu.generation(), context)
                .flatMap(targetType -> findActiveTemplate(context, targetType)
                    .filter(template -> getFormResponseUseCase
                        .findSubmittedByFormIdAndRespondentMemberId(template.getFormId(), requesterMemberId)
                        .isEmpty())
                    .map(template -> UserFeedbackTemplateInfo.builder()
                        .templateId(template.getId())
                        .context(template.getContext())
                        .targetType(template.getTargetType())
                        .form(getFormUseCase.getFormWithStructure(template.getFormId()))
                        .build())));
    }

    private Optional<UserFeedbackTemplate> findActiveTemplate(
        UserFeedbackContext context,
        UserFeedbackTargetType targetType
    ) {
        Optional<UserFeedbackTemplate> template = loadUserFeedbackTemplatePort.findByContextAndTargetType(context, targetType);
        if (targetType != UserFeedbackTargetType.NEW_CHALLENGER
            && targetType != UserFeedbackTargetType.EXPERIENCED_CHALLENGER) {
            return template;
        }
        return template.or(() -> loadUserFeedbackTemplatePort.findByContextAndTargetType(
            context, UserFeedbackTargetType.CHALLENGER));
    }

    /**
     * 요청한 설문 종류와 사용자 이력에 따라 설문 대상을 판별합니다.
     * <p>
     * 설문 종류별 판별 기준:
     * <ol>
     *   <li>매칭 설문: 전체 사용자 대상(ALL)으로 분류합니다.</li>
     *   <li>운영진 설문: 현재 기수 중앙 운영진 또는 최고 관리자이면 운영진 대상(ADMIN)으로 분류하고, 아니면 제외합니다.</li>
     *   <li>지원 설문: 현재 기수 챌린저만 대상으로 하며, 다른 기수 이력도 있으면 기존 챌린저(EXPERIENCED_CHALLENGER)로 분류합니다.</li>
     *   <li>지원 설문의 10기 예외: 기획·디자인 매칭 경험을 반영해 기획 파트는 기존 챌린저(EXPERIENCED_CHALLENGER)로 분류합니다.</li>
     *   <li>지원 설문의 나머지 대상자는 신규 챌린저(NEW_CHALLENGER)로 분류합니다.</li>
     * </ol>
     */
    private Optional<UserFeedbackTargetType> resolveTargetType(
        Long memberId,
        Long activeGisuId,
        Long generation,
        UserFeedbackContext context
    ) {
        return switch (context) {
            case APPLICATION_SUBMITTED -> resolveApplicationTargetType(memberId, activeGisuId, generation);
            case MATCHING_COMPLETED -> resolveMatchingTargetType();
            case APPLICATION_MONITORING -> resolveMonitoringTargetType(memberId, activeGisuId);
        };
    }

    private Optional<UserFeedbackTargetType> resolveMatchingTargetType() {
        return Optional.of(UserFeedbackTargetType.ALL);
    }

    private Optional<UserFeedbackTargetType> resolveMonitoringTargetType(Long memberId, Long activeGisuId) {
        return getChallengerRoleUseCase.isCentralMemberInGisu(memberId, activeGisuId)
            ? Optional.of(UserFeedbackTargetType.ADMIN) : Optional.empty();
    }

    private Optional<UserFeedbackTargetType> resolveApplicationTargetType(Long memberId, Long activeGisuId, Long generation) {
        List<ChallengerInfo> allHistory = getChallengerUseCase.getAllByMemberId(memberId);

        boolean isActiveChallenger = allHistory.stream()
            .anyMatch(c -> c.gisuId().equals(activeGisuId));
        if (!isActiveChallenger) {
            return Optional.empty();
        }

        boolean hasPreviousGisu = allHistory.stream()
            .anyMatch(c -> !c.gisuId().equals(activeGisuId));
        if (hasPreviousGisu) {
            return Optional.of(UserFeedbackTargetType.EXPERIENCED_CHALLENGER);
        }

        // 10기 한정: 기획-디자인 매칭을 경험한 PM은 챌린저 이력이 이번 기수 하나뿐이더라도 EXPERIENCED_CHALLENGER
        if (Long.valueOf(10).equals(generation)) {
            boolean isPm = allHistory.stream()
                .filter(c -> c.gisuId().equals(activeGisuId))
                .anyMatch(c -> c.part() == ChallengerPart.PLAN);
            if (isPm) {
                return Optional.of(UserFeedbackTargetType.EXPERIENCED_CHALLENGER);
            }
        }

        return Optional.of(UserFeedbackTargetType.NEW_CHALLENGER);
    }
}
