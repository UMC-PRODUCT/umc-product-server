package com.umc.product.authorization.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 사용자의 역할 정보를 조회하는 UseCase
 * <p>
 * Notice, Curriculum 등 다른 도메인에서 사용자의 역할을 확인할 때 사용합니다.
 */
public interface GetMemberRolesUseCase {

    List<ChallengerRoleInfo> getRoles(Long memberId);


    /**
     * 사용자의 모든 역할 타입을 조회
     *
     * @param memberId 사용자 ID
     * @return 역할 타입 리스트 (중복 제거됨)
     */
    List<ChallengerRoleType> getRoleTypes(Long memberId);

    /**
     * 특정 역할을 가지고 있는지 확인
     *
     * @param memberId 사용자 ID
     * @param role     확인할 역할
     * @return 역할이 있으면 true
     */
    boolean hasRole(Long memberId, ChallengerRoleType role);

    /**
     * 여러 역할 중 하나라도 가지고 있는지 확인
     *
     * @param memberId 사용자 ID
     * @param roles    확인할 역할들
     * @return 역할 중 하나라도 있으면 true
     */
    boolean hasAnyRole(Long memberId, ChallengerRoleType... roles);

    /**
     * 모든 역할을 가지고 있는지 확인
     *
     * @param memberId 사용자 ID
     * @param roles    확인할 역할들
     * @return 모든 역할을 가지고 있으면 true
     */
    boolean hasAllRoles(Long memberId, ChallengerRoleType... roles);

    /**
     * 중앙운영사무국 총괄단 여부 (총괄, 부총괄)
     *
     * @param memberId 사용자 ID
     * @return 총괄단이면 true
     */
    boolean isCentralCore(Long memberId);

    /**
     * 중앙운영사무국 멤버 여부 (총괄단, 운영국원, 교육국원)
     *
     * @param memberId 사용자 ID
     * @return 중앙 멤버면 true
     */
    boolean isCentralMember(Long memberId);

    /**
     * 특정 학교의 회장단 여부 (회장, 부회장)
     *
     * @param memberId 사용자 ID
     * @param schoolId 학교 ID
     * @return 해당 학교 회장단이면 true
     */
    boolean isSchoolCore(Long memberId, Long schoolId);

    /**
     * 특정 학교의 관리자 여부 (회장단, 파트장, 기타 운영진)
     *
     * @param memberId 사용자 ID
     * @param schoolId 학교 ID
     * @return 해당 학교 관리자면 true
     */
    boolean isSchoolAdmin(Long memberId, Long schoolId);

    /**
     * 특정 지부의 지부장 여부
     *
     * @param memberId  사용자 ID
     * @param chapterId 지부 ID
     * @return 해당 지부 지부장이면 true
     */
    boolean isChapterPresident(Long memberId, Long chapterId);

    /**
     * 특정 기수에서의 역할 타입 조회
     *
     * @param memberId 사용자 ID
     * @param gisuId   기수 ID
     * @return 해당 기수에서의 역할 타입 리스트
     */
    List<ChallengerRoleType> getRolesByGisu(Long memberId, Long gisuId);

    /**
     * 특정 학교에서의 역할 타입 조회
     *
     * @param memberId 사용자 ID
     * @param schoolId 학교 ID
     * @return 해당 학교에서의 역할 타입 리스트
     */
    List<ChallengerRoleType> getRolesBySchool(Long memberId, Long schoolId);

    /**
     * 특정 지부에서의 역할 타입 조회
     *
     * @param memberId  사용자 ID
     * @param chapterId 지부 ID
     * @return 해당 지부에서의 역할 타입 리스트
     */
    List<ChallengerRoleType> getRolesByChapter(Long memberId, Long chapterId);

    /**
     * 특정 기수에서 특정 역할을 가지고 있는지 확인
     *
     * @param memberId 사용자 ID
     * @param gisuId   기수 ID
     * @param role     확인할 역할
     * @return 해당 기수에서 역할이 있으면 true
     */
    boolean hasRoleInGisu(Long memberId, Long gisuId, ChallengerRoleType role);

    /**
     * 특정 기수에서 여러 역할 중 하나라도 가지고 있는지 확인
     *
     * @param memberId 사용자 ID
     * @param gisuId   기수 ID
     * @param roles    확인할 역할들
     * @return 해당 기수에서 역할 중 하나라도 있으면 true
     */
    boolean hasAnyRoleInGisu(Long memberId, Long gisuId, ChallengerRoleType... roles);

    /**
     * 특정 기수에서 모든 역할을 가지고 있는지 확인
     *
     * @param memberId 사용자 ID
     * @param gisuId   기수 ID
     * @param roles    확인할 역할들
     * @return 해당 기수에서 모든 역할을 가지고 있으면 true
     */
    boolean hasAllRolesInGisu(Long memberId, Long gisuId, ChallengerRoleType... roles);

    /**
     * 특정 기수에서 중앙운영사무국 총괄단 여부 (총괄, 부총괄)
     *
     * @param memberId 사용자 ID
     * @param gisuId   기수 ID
     * @return 해당 기수에서 총괄단이면 true
     */
    boolean isCentralCoreInGisu(Long memberId, Long gisuId);

    /**
     * 특정 기수에서 중앙운영사무국 멤버 여부 (총괄단, 운영국원, 교육국원)
     *
     * @param memberId 사용자 ID
     * @param gisuId   기수 ID
     * @return 해당 기수에서 중앙 멤버면 true
     */
    boolean isCentralMemberInGisu(Long memberId, Long gisuId);

    /**
     * 여러 챌린저의 역할 타입을 일괄 조회
     *
     * @param challengerIds 챌린저 ID 목록
     * @return 챌린저 ID → 역할 타입 리스트
     */
    Map<Long, List<ChallengerRoleType>> getRoleTypesByChallengerIds(Set<Long> challengerIds);
}
