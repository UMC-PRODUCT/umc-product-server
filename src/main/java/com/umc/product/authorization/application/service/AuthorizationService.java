package com.umc.product.authorization.application.service;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.query.GetSubjectAttributesUseCase;
import com.umc.product.authorization.application.port.out.LoadChallengerRolePort;
import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class AuthorizationService implements CheckPermissionUseCase, GetSubjectAttributesUseCase {

    private final LoadChallengerRolePort loadChallengerRolePort;
    private final Map<ResourceType, ResourcePermissionEvaluator> evaluators;

    private final GetMemberUseCase getMemberUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    /**
     * ResourcePermissionEvaluator에 대한 생성자 주입
     * <p>
     * 셍성자에서 List를 Map으로 변환하기 위해서 Lombok을 사용하지 않았음.
     */
    public AuthorizationService(LoadChallengerRolePort loadChallengerRolePort,
                                List<ResourcePermissionEvaluator> evaluatorList, GetMemberUseCase getMemberUseCase,
                                GetChapterUseCase getChapterUseCase, GetChallengerUseCase getChallengerUseCase) {
        this.loadChallengerRolePort = loadChallengerRolePort;
        this.getMemberUseCase = getMemberUseCase;
        this.getChapterUseCase = getChapterUseCase;
        this.getChallengerUseCase = getChallengerUseCase;
        this.evaluators = evaluatorList.stream()
            .collect(Collectors.toMap(
                ResourcePermissionEvaluator::supportedResourceType,
                Function.identity()
            ));

        log.info("등록된 ResourcePermissionEvaluator: {}", evaluators.keySet());
    }

    @Override
    public boolean check(Long memberId, ResourcePermission permission) {
        log.info("권한 평가 시작");

        ResourcePermissionEvaluator evaluator = evaluators.get(permission.resourceType());
        if (evaluator == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.NO_EVALUATOR_MATCHING_RESOURCE_TYPE,
                "Evaluator for Resource Type [" + permission.resourceType() + "] not found.");
        }

        SubjectAttributes subjectAttributes = getByMemberId(memberId);

        log.info("Subject Attribute {}가 평가를 요청했습니다.", subjectAttributes.toString());

        boolean hasPermission = evaluator.evaluate(subjectAttributes, permission);

        log.debug("Permission check - memberId: {}, resource: {}:{}, permission: {}, result: {}",
            memberId, permission.resourceType(), permission.resourceId(),
            permission.permission(), hasPermission);

        return hasPermission;
    }

    @Override
    public SubjectAttributes getByMemberId(Long memberId) {
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);
        Long schoolId = memberInfo.schoolId();

        List<ChallengerInfo> memberChallengerList = getChallengerUseCase.getAllByMemberId(memberId);
        List<GisuChallengerInfo> gisuInfos = memberChallengerList.stream()
            .map(challengerInfo -> GisuChallengerInfo.builder()
                .gisuId(challengerInfo.gisuId())
                .chapterId(getChapterUseCase.byGisuAndSchool(challengerInfo.gisuId(), schoolId).id())
                .part(challengerInfo.part())
                .challengerId(challengerInfo.challengerId())
                .build())
            .toList();

        List<RoleAttribute> roles = loadChallengerRolePort.findByMemberId(memberId)
            .stream().map(RoleAttribute::from).toList();

        return SubjectAttributes.builder()
            .memberId(memberId)
            .schoolId(schoolId)
            .gisuChallengerInfos(gisuInfos)
            .roleAttributes(roles)
            .build();
    }

    @Override
    public void checkOrThrow(Long memberId, ResourcePermission permission) {
        if (!check(memberId, permission)) {
            log.warn("Permission denied - memberId: {}, resource: {}:{}, permission: {}",
                memberId, permission.resourceType(), permission.resourceId(), permission.permission());

            throw new AuthorizationDomainException(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED);
        }
    }
}
