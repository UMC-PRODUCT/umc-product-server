package com.umc.product.project.application.access;

import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.Set;

/**
 * 프로젝트 조회 시 적용되는 가시 범위(scope) typed value.
 * <p>
 * 권한 판정(L2 {@code ProjectPermissionEvaluator})이 binary("진입 가능한가")만 다룬다면, scope 는
 * "어떤 부분집합을 보여줄지"를 표현한다. 같은 사용자라도 호출 의도(공개 검색 vs 관리 화면)에 따라
 * 다른 scope 가 적용될 수 있다.
 */
public sealed interface ProjectAccessScope {

    /** 모든 프로젝트 노출 (Central Core). 상태 필터는 호출자가 요청한 그대로 통과. */
    record All(Set<ProjectStatus> visibleStatuses) implements ProjectAccessScope {}

    /** 특정 지부의 프로젝트만 노출 (지부장). */
    record ChapterScoped(Long chapterId, Set<ProjectStatus> visibleStatuses) implements ProjectAccessScope {}

    /** 특정 학교의 프로젝트만 노출 (학교 회장단). */
    record SchoolScoped(Long schoolId, Set<ProjectStatus> visibleStatuses) implements ProjectAccessScope {}

    /** 본인이 PM 인 프로젝트만 노출 (PM 챌린저, 관리 화면). */
    record OwnerOnly(Long memberId, Set<ProjectStatus> visibleStatuses) implements ProjectAccessScope {}

    /** 일반 챌린저용 공개 목록 ({@link ProjectStatus#IN_PROGRESS} 만). */
    record PublicOnly() implements ProjectAccessScope {}

    /** 관리 대상 0건 (일반 챌린저가 관리 화면 호출 시 등). */
    record None() implements ProjectAccessScope {}
}
