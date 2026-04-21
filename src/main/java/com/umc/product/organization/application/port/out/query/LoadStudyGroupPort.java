package com.umc.product.organization.application.port.out.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupViewScope;
import com.umc.product.organization.domain.StudyGroup;
import java.util.List;
import java.util.Set;

public interface LoadStudyGroupPort {

    StudyGroup findById(Long id);

    StudyGroup findByName(String name);

    /**
     * 3단계: 스터디 그룹 목록 조회 (cursor 기반, 활성 기수 기준)
     * 페이지네이션 처리는 Controller에서 CursorResponse.of()로 수행
     */
    List<StudyGroupListInfo.StudyGroupInfo> findStudyGroups(Long schoolId, ChallengerPart part, Long cursor, int size);

    /**
     * 운영진(회장단, 파트장)이 챌린저 스터디 그룹 목록 조회 (역할 Scope 기반).
     * <p>
     * scopes를 OR로 합쳐 EXISTS 서브쿼리로 필터링한다. scopes가 null/빈 리스트인 경우
     * 구현체는 DB를 조회하지 않고 즉시 빈 리스트를 반환해야 한다(불필요한 풀 스캔 방지).
     * 정렬은 id DESC(신규 우선), 커서는 {@code studyGroup.id.lt(cursor)} 로 적용된다.
     *
     * @param scopes 역할 기반 조회 범위. 비어있으면 빈 리스트 반환.
     * @param gisuId 활성 기수 ID (조회 대상 기수)
     * @param cursor 직전 페이지 마지막 groupId. 첫 페이지는 null.
     * @param size   조회 사이즈 (hasNext 판단용 +1 포함)
     * @return Scope 범위 내의 스터디 그룹 목록
     */
    List<StudyGroupListInfo.StudyGroupInfo> findMyStudyGroups(
        List<StudyGroupViewScope> scopes, Long gisuId, Long cursor, int size);
    List<StudyGroupNameInfo> findStudyGroupNames(Long schoolId, ChallengerPart part);

    /**
     * 4단계: 스터디 그룹 상세 조회
     */
    StudyGroupDetailInfo findStudyGroupDetail(Long groupId);

    /**
     * 특정 기수에서 해당 파트들의 스터디 그룹 ID 목록 조회 (파트장용)
     */
    List<Long> findIdsByGisuIdAndPartIn(Long gisuId, Set<ChallengerPart> parts);

    /**
     * 지정한 기수/파트에서 이미 다른 스터디 그룹에 소속된 멤버 ID를 반환한다.
     * <p>
     * 스터디 그룹 생성/멤버 추가 시 "동일 기수+파트 내 중복 소속" 여부를 서비스가 검증하기 위한 조회이다.
     * 반환 집합이 비어있으면 모두 신규 가입 가능한 멤버이다.
     *
     * @param id        기수 ID
     * @param part      파트
     * @param longs     검사 대상 memberId 집합
     * @return 이미 다른 그룹에 소속된 memberId 집합 (비어있으면 충돌 없음)
     */
    Set<Long> findConflictedMemberIds(Long id, ChallengerPart part, Set<Long> longs);
}
