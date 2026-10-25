package com.umc.product.project.application.access;

import java.util.Set;

import com.umc.product.project.domain.enums.ProjectStatus;

/**
 * 프로젝트 조회 시 적용되는 가시 범위(scope) typed value.
 * <p>
 * 권한 판정(L2 {@code ProjectPermissionEvaluator})이 binary("진입 가능한가")만 다룬다면, scope 는
 * "어떤 부분집합을 보여줄지"를 표현한다. 같은 사용자라도 호출 의도(공개 검색 vs 관리 화면)에 따라
 * 다른 scope 가 적용될 수 있다.
 */
public sealed interface ProjectAccessScope {

    /** 전체 지부에서 권한 판정으로 허용한 상태의 프로젝트를 노출한다. */
    record All(Set<ProjectStatus> visibleStatuses) implements ProjectAccessScope {}

    /** 특정 지부의 프로젝트만 노출 (지부장). */
    record ChapterScoped(Long chapterId, Set<ProjectStatus> visibleStatuses) implements ProjectAccessScope {}

    /** 지부장·중앙운영진 겸직: 본인 지부 범위에 전체 지부의 승인대기·등록완료를 합친다. */
    record ChapterWithGlobalRead(
        Long chapterId, Set<ProjectStatus> visibleStatuses, Set<ProjectStatus> globalVisibleStatuses
    ) implements ProjectAccessScope {}

    /** 본인이 PM 인 프로젝트만 노출 (PM 챌린저, 관리 화면). */
    record OwnerOnly(Long memberId, Set<ProjectStatus> visibleStatuses) implements ProjectAccessScope {}

    /**
     * 상위 권한 scope 결과에 본인 PO 프로젝트를 추가 포함한다.
     * <p>
     * 운영진/지부장 scope 에서 DRAFT 를 전체 공개하지 않으면서, 본인이 PO 인 프로젝트만 별도 OR 조건으로 노출하기 위한 값 객체.
     */
    record WithOwnerIncluded(
        ProjectAccessScope baseScope,
        Long ownerMemberId,
        Set<ProjectStatus> ownerVisibleStatuses
    ) implements ProjectAccessScope {}

    /** 일반 챌린저용 공개 목록 ({@link ProjectStatus#IN_PROGRESS} / {@link ProjectStatus#COMPLETED}). */
    record PublicOnly() implements ProjectAccessScope {}

    /** 관리 대상 0건 (일반 챌린저가 관리 화면 호출 시 등). */
    record None() implements ProjectAccessScope {}
}
