package com.umc.product.authorization.application.service;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
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
import com.umc.product.member.application.port.in.query.MemberInfo;
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
public class AuthorizationService implements CheckPermissionUseCase {

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

        // 리소스 유형에 맞는 권한 평가기를 선택함. 없다면 에러 발생
        ResourcePermissionEvaluator evaluator = evaluators.get(permission.resourceType());
        if (evaluator == null) {
            throw new AuthorizationDomainException(AuthorizationErrorCode.NO_EVALUATOR_MATCHING_RESOURCE_TYPE,
                "Evaluator for Resource Type [" + permission.resourceType() + "] not found.");
        }

        // 사용자가 활동한 모든 기수를 확인
        // 해당 기수마다 chapterId, challengerRoleId를 가져옴
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);

        // 학교 ID는 회원정보에 저장되어 있음
        Long schoolId = memberInfo.schoolId();

        // memberId로 사용자와 관련된 모든 challenger를 가지고 옴
        // 그 challenger를 기반으로 사용자가 활동했던 모든 기수를 가져옴.
        // 그러면 기수와 학교를 조합해서 챕터들이 나오겠지? 굳 그거 쓰면 될듯
        List<ChallengerInfo> memberChallengerList = getChallengerUseCase.getMemberChallengerList(memberId);
        List<GisuChallengerInfo> chapterIds = memberChallengerList.stream().map((challengerInfo) ->
            GisuChallengerInfo.builder()
                .gisuId(challengerInfo.gisuId())
                .chapterId(getChapterUseCase.byGisuAndSchool(challengerInfo.gisuId(), schoolId).id())
                .part(challengerInfo.part())
                .challengerId(challengerInfo.challengerId())
                .build()
        ).toList();
        List<RoleAttribute> roles = loadChallengerRolePort.findByMemberId(memberId)
            .stream().map(RoleAttribute::from).toList();

        SubjectAttributes subjectAttributes = SubjectAttributes.builder()
            .memberId(memberId)
            .schoolId(schoolId)
            .gisuChallengerInfos(chapterIds)
            .roleAttributes(roles)
            .build();

        log.info("Subject Attribute {}가 평가를 요청했습니다.", subjectAttributes.toString());

        // 평가기로 평가
        boolean hasPermission = evaluator.evaluate(subjectAttributes, permission);

        log.debug("Permission check - memberId: {}, roles: {}, resource: {}:{}, permission: {}, result: {}",
            memberId, roles, permission.resourceType(), permission.resourceId(),
            permission.permission(), hasPermission);

        return hasPermission;
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
