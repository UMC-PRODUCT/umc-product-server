package com.umc.product.organization.application.port.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupViewScope;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyGroupQueryService implements GetStudyGroupUseCase {

    private final LoadStudyGroupPort loadStudyGroupPort;
    private final GetFileUseCase getFileUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    /**
     * 내 스터디 그룹 목록 조회 UseCase 구현.
     * <p>
     * 사용자의 활성 기수에서의 역할(Role)을 바탕으로 조회 범위(Scope)를 조립하여,
     * 해당 Scope가 허용하는 스터디 그룹 목록을 커서 페이지네이션으로 반환한다.
     * <ul>
     *   <li>학교 회장단(SCHOOL_PRESIDENT/VICE) → 해당 학교 멤버가 포함된 모든 스터디 그룹</li>
     *   <li>파트장(SCHOOL_PART_LEADER) → 본인이 운영진(Organizer)으로 등록된 스터디 그룹</li>
     *   <li>일반 챌린저/SCHOOL_ETC_ADMIN → 조회 권한 없음(빈 리스트 반환)</li>
     * </ul>
     * 사용자가 여러 역할을 동시에 보유할 수 있으므로 Scope는 리스트로 넘기고, Repository에서 OR로 합쳐 필터링한다.
     * <p>
     * fetchSize 는 Controller의 CursorResponse.of()가 hasNext를 판단할 수 있도록 요청 size + 1 로 조회한다.
     *
     * @param memberId 요청 주체(로그인 사용자)의 memberId
     * @param cursor   직전 페이지 마지막 groupId (신규 정렬이 id DESC 이므로 lt(cursor)로 필터링). 첫 페이지는 null.
     * @param size     페이지 크기(실제 조회는 size+1 로 수행하여 hasNext 판단)
     * @return 스터디 그룹 목록. 권한 scope가 하나도 없으면 빈 리스트.
     */
    @Override
    public List<StudyGroupListInfo.StudyGroupInfo> getMyStudyGroups(Long memberId, Long cursor, int size) {
        Long schoolId = getMemberUseCase.getById(memberId).schoolId();
        Long activeGisuId = getGisuUseCase.getActiveGisuId();

        List<StudyGroupViewScope> scopes = resolveScopes(memberId, activeGisuId, schoolId);
        if (scopes.isEmpty()) {
            return List.of();
        }

        List<StudyGroupListInfo.StudyGroupInfo> groups =
            loadStudyGroupPort.findMyStudyGroups(scopes, activeGisuId, cursor, size + 1);

        return resolveStudyGroupListUrls(groups);
    }

    /**
     * 사용자의 활성 기수 내 역할을 검사하여 조회 가능한 Scope 리스트를 구성한다.
     * <p>
     * 새로운 역할이 추가될 경우 이 메서드에만 분기를 더하면 되어 확장 지점이 단일화된다.
     * 학교 회장단 Scope 구성 시 Member 도메인에서 학교 멤버 ID 집합을 조회하는데,
     * 집합이 비어있다면 EXISTS 서브쿼리에서 항상 false가 되므로 Scope 자체를 추가하지 않아 쿼리 비용을 절감한다.
     *
     * @param memberId 요청 주체 memberId
     * @param gisuId   활성 기수 ID
     * @param schoolId 사용자의 학교 ID (회장단 Scope에서 사용)
     * @return 조회 가능한 Scope 리스트. 권한이 전혀 없으면 빈 리스트.
     */
    private List<StudyGroupViewScope> resolveScopes(Long memberId, Long gisuId, Long schoolId) {
        List<StudyGroupViewScope> scopes = new ArrayList<>();

        if (getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)) {
            Set<Long> schoolMemberIds = getMemberUseCase.findAllIdsBySchoolId(schoolId);
            if (!schoolMemberIds.isEmpty()) {
                scopes.add(new StudyGroupViewScope.AsSchoolCore(schoolMemberIds));
            }
        }

        if (getChallengerRoleUseCase.hasRoleTypeInGisu(memberId, gisuId, ChallengerRoleType.SCHOOL_PART_LEADER)) {
            scopes.add(new StudyGroupViewScope.AsPartLeader(memberId));
        }

        return scopes;
    }

    /**
     * 권한(역할) 기반 스터디 그룹 이름 목록 조회.
     * <p>
     * {@link #getMyStudyGroups} 와 동일한 Scope 해석 규칙을 사용한다.
     * <ul>
     *   <li>학교 회장단 → 학교 멤버가 포함된 모든 그룹의 이름</li>
     *   <li>파트장 → 본인이 운영진인 그룹의 이름</li>
     *   <li>권한 없음 → 빈 리스트</li>
     * </ul>
     * 페이징 없이 전체 결과(groupId, name)를 반환하므로 토글/드롭다운 UI에 바로 쓸 수 있다.
     */
    @Override
    public List<StudyGroupNameInfo> getStudyGroupNames(Long memberId) {
        Long schoolId = getMemberUseCase.getById(memberId).schoolId();
        Long activeGisuId = getGisuUseCase.getActiveGisuId();

        List<StudyGroupViewScope> scopes = resolveScopes(memberId, activeGisuId, schoolId);
        if (scopes.isEmpty()) {
            return List.of();
        }

        return loadStudyGroupPort.findStudyGroupNames(scopes, activeGisuId);
    }

    @Override
    public List<Long> getStudyGroupIdsByParts(Long gisuId, Set<ChallengerPart> parts) {
        return loadStudyGroupPort.findIdsByGisuIdAndPartIn(gisuId, parts);
    }

    /**
     * 스터디 그룹 ID 로 소속 스터디원 목록 조회.
     * <p>
     * Repository 에서 Member/School 도메인까지 JOIN 하여 (memberId, 학교명, 프로필 이미지 ID) 를 가져온 뒤,
     * 프로필 이미지 ID 를 일괄 URL 로 치환해 반환한다. 조회 결과가 비어있으면 storage 호출 자체를 생략한다.
     */
    @Override
    public List<StudyGroupMemberInfo> getStudyGroupMembers(Long groupId) {
        List<StudyGroupMemberInfo> members = loadStudyGroupPort.findStudyGroupMembers(groupId);
        if (members.isEmpty()) {
            return members;
        }

        Set<String> imageIds = new LinkedHashSet<>();
        for (StudyGroupMemberInfo m : members) {
            if (m.profileImageUrl() != null) {
                imageIds.add(m.profileImageUrl());
            }
        }
        if (imageIds.isEmpty()) {
            return members;
        }

        Map<String, String> urlMap = resolveProfileImageUrls(imageIds);
        return members.stream()
            .map(m -> new StudyGroupMemberInfo(
                m.memberId(),
                m.schoolName(),
                urlMap.getOrDefault(m.profileImageUrl(), m.profileImageUrl())))
            // Map의 Key는 fileId, Value는 URL. 치환 실패 시 원래 fileId 반환
            // 지금은 fileId와 치환 성공한 URL이 모두 같은 profileImageUrl 필드에 담겨 있다. 구별 처리 예정

            .toList();
    }

    private List<StudyGroupListInfo.StudyGroupInfo> resolveStudyGroupListUrls(
            List<StudyGroupListInfo.StudyGroupInfo> groups) {
        Set<String> imageIds = new LinkedHashSet<>();
        for (StudyGroupListInfo.StudyGroupInfo group : groups) {
            for (StudyGroupListInfo.StudyGroupInfo.Organizer organizer : group.organizers()) {
                if (organizer.profileImageUrl() != null) {
                    imageIds.add(organizer.profileImageUrl());
                }
            }
            for (StudyGroupListInfo.StudyGroupInfo.Member m : group.members()) {
                if (m.profileImageUrl() != null) {
                    imageIds.add(m.profileImageUrl());
                }
            }
        }

        if (imageIds.isEmpty()) {
            return groups;
        }

        Map<String, String> urlMap = resolveProfileImageUrls(imageIds);

        return groups.stream()
                .map(group -> new StudyGroupListInfo.StudyGroupInfo(
                        group.groupId(),
                        group.name(),
                        group.organizers().stream()
                                .map(o -> new StudyGroupListInfo.StudyGroupInfo.Organizer(
                                        o.memberId(),
                                        o.name(),
                                        urlMap.getOrDefault(o.profileImageUrl(), o.profileImageUrl())
                                ))
                                .toList(),
                        group.members().stream()
                                .map(m -> new StudyGroupListInfo.StudyGroupInfo.Member(
                                        m.memberId(),
                                        m.name(),
                                        urlMap.getOrDefault(m.profileImageUrl(), m.profileImageUrl())
                                ))
                                .toList()
                ))
                .toList();
    }

    private StudyGroupDetailInfo resolveStudyGroupDetailUrls(StudyGroupDetailInfo detail) {
        Set<String> imageIds = new LinkedHashSet<>();
        if (detail.leader() != null && detail.leader().profileImageUrl() != null) {
            imageIds.add(detail.leader().profileImageUrl());
        }
        for (StudyGroupDetailInfo.MemberInfo member : detail.members()) {
            if (member.profileImageUrl() != null) {
                imageIds.add(member.profileImageUrl());
            }
        }

        if (imageIds.isEmpty()) {
            return detail;
        }

        Map<String, String> urlMap = resolveProfileImageUrls(imageIds);

        StudyGroupDetailInfo.MemberInfo resolvedLeader = detail.leader() == null ? null
                : new StudyGroupDetailInfo.MemberInfo(
                        detail.leader().challengerId(),
                        detail.leader().memberId(),
                        detail.leader().name(),
                        urlMap.getOrDefault(detail.leader().profileImageUrl(),
                                detail.leader().profileImageUrl())
                );

        List<StudyGroupDetailInfo.MemberInfo> resolvedMembers = detail.members().stream()
                .map(m -> new StudyGroupDetailInfo.MemberInfo(
                        m.challengerId(),
                        m.memberId(),
                        m.name(),
                        urlMap.getOrDefault(m.profileImageUrl(), m.profileImageUrl())
                ))
                .toList();

        return new StudyGroupDetailInfo(
                detail.groupId(),
                detail.name(),
                detail.part(),
                detail.schools(),
                detail.createdAt(),
                detail.memberCount(),
                resolvedLeader,
                resolvedMembers
        );
    }

    private Map<String, String> resolveProfileImageUrls(Set<String> profileImageIds) {
        return getFileUseCase.getFileLinks(List.copyOf(profileImageIds));
    }
}
