package com.umc.product.organization.application.port.out.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupNameInfo;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupViewScope;
import com.umc.product.organization.domain.StudyGroup;
import java.util.List;
import java.util.Set;

public interface LoadStudyGroupPort {
    // TODO: 의존성 역전 있음, 수정 필요

    StudyGroup getById(Long id);

    StudyGroup getByName(String name);

    /**
     * 운영진(회장단, 파트장)이 챌린저 스터디 그룹 목록 조회 (역할 Scope 기반).
     * <p>
     * scopes를 OR로 합쳐 EXISTS 서브쿼리로 필터링한다. scopes가 null/빈 리스트인 경우 정렬은 id DESC(신규 우선), 커서는
     * {@code studyGroup.id.lt(cursor)} 로 적용된다.
     *
     * @param scopes 역할 기반 조회 범위. 비어있으면 빈 리스트 반환.
     * @param gisuId 활성 기수 ID (조회 대상 기수)
     * @param cursor 직전 페이지 마지막 groupId. 첫 페이지는 null.
     * @param size   조회 사이즈 (hasNext 판단용 +1 포함)
     * @return Scope 범위 내의 스터디 그룹 목록
     */
    List<StudyGroupInfo> findMyStudyGroups(
        List<StudyGroupViewScope> scopes, Long gisuId, Long cursor, int size);

    /**
     * 운영진(회장단/파트장) 권한에 따라 활성 기수의 스터디 그룹 이름 목록을 조회한다.
     * <p>
     * {@link #findMyStudyGroups} 와 동일한 Scope OR 합성 규칙을 사용하지만, 페이지네이션과 운영진/멤버 상세 조립 없이 (groupId, name) 만 반환 (토글/드롭다운
     * 용도). scopes가 null/빈 리스트이면 구현체는 즉시 빈 리스트를 반환해야 한다.
     *
     * @param scopes 역할 기반 조회 범위
     * @param gisuId 활성 기수 ID
     * @return Scope 범위 내 스터디 그룹의 (id, name) 목록
     */
    List<StudyGroupNameInfo> findStudyGroupNames(List<StudyGroupViewScope> scopes, Long gisuId);

    /**
     * 스터디 그룹 ID 로 해당 그룹의 스터디원(멤버) 목록을 조회한다.
     * <p>
     * study_group_member 테이블 기준으로 소속된 멤버를 가져오며, Member/School 도메인과 JOIN 하여 (memberId, 학교명, 프로필 이미지 ID) 를 함께 싣는다. 프로필
     * 이미지 ID → URL 치환은 Service 에서 수행한다.
     *
     * @param studyGroupId 스터디 그룹 ID
     * @return 그룹에 소속된 스터디원 목록 (소속 없으면 빈 리스트)
     */
    List<StudyGroupMemberInfo> findStudyGroupMembers(Long studyGroupId);

    List<StudyGroupMemberInfo> findStudyGroupMentors(Long studyGroupId);

    /**
     * 특정 기수에서 해당 파트들의 스터디 그룹 ID 목록 조회 (파트장용)
     */
    List<Long> findIdsByGisuIdAndPartIn(Long gisuId, Set<ChallengerPart> parts);

    /**
     * 지정한 기수/파트에서 이미 다른 스터디 그룹에 소속된 멤버 ID를 반환한다.
     * <p>
     * 스터디 그룹 생성/멤버 추가 시 "동일 기수+파트 내 중복 소속" 여부를 서비스가 검증하기 위한 조회이다. 반환 집합이 비어있으면 모두 신규 가입 가능한 멤버이다.
     *
     * @param id        기수 ID
     * @param part      파트
     * @param memberIds 검사 대상 memberId 집합
     * @return 이미 다른 그룹에 소속된 memberId 집합 (비어있으면 충돌 없음)
     */
    Set<Long> findConflictedMemberIds(Long id, ChallengerPart part, Set<Long> memberIds);
}
