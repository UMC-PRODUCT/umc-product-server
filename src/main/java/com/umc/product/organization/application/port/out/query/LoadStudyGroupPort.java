package com.umc.product.organization.application.port.out.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
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
     * 스터디 그룹 이름 목록 조회 (활성 기수 기준, 학교/파트 필터)
     * 내 스터디 그룹 목록 조회 (역할 Scope 기반).
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
     */
    List<StudyGroupNameInfo> findStudyGroupNames(Long schoolId, ChallengerPart part);

    /**
     * 4단계: 스터디 그룹 상세 조회
     */
    StudyGroupDetailInfo findStudyGroupDetail(Long groupId);

    /**
     * 특정 기수에서 해당 파트들의 스터디 그룹 ID 목록 조회 (파트장용)
     */
    List<Long> findIdsByGisuIdAndPartIn(Long gisuId, Set<ChallengerPart> parts);

    Set<Long> findConflictedMemberIds(Long id, ChallengerPart part, Set<Long> longs);
}
