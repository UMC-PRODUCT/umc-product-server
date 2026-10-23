package com.umc.product.project.application.access;

import java.util.List;

/**
 * 프로젝트 지원서 목록 조회 시 적용되는 가시 범위(scope) typed value.
 * <p>
 * 권한 판정(L2 {@code ProjectApplicationPermissionEvaluator})이 단건 binary 만 다룬다면, scope 는 "어떤 부분집합을 보여줄지"를 표현한다. 같은 사용자라도 호출
 * 의도(본인 지원 내역 vs PO 의 지원자 목록 vs 운영진 모니터링)에 따라 다른 scope 가 적용된다.
 */
public sealed interface ProjectApplicationAccessScope {

    /**
     * 본인이 지원자인 지원서만 노출 (본인 지원 내역 화면).
     */
    record OwnerOnly(Long memberId) implements ProjectApplicationAccessScope {
    }

    /**
     * 특정 프로젝트의 지원서만 (PO/Sub-PM/CC/지부장/학교장의 "이 프로젝트 지원자 목록")
     * <p>
     * 두 플래그는 "지원 기간이 아직 안 끝난 차수"를 얼마나 보여줄지를 나눈다.
     * <ul>
     *   <li>{@code includeOngoingMatchingRounds}: 지원자 <b>목록</b>에 진행 중인 차수의 지원서를 포함한다.</li>
     *   <li>{@code includeOngoingApplicationDetails}: 진행 중인 차수 지원서의 <b>내용</b>(답변·첨부)까지 열람한다.</li>
     * </ul>
     * PM·보조 PM은 지원 기간에도 제출된 지원자 목록은 실시간으로 보지만, 지원서 열람은 지원 마감 후부터다.
     * 최고관리자와 같은 기수 총괄단은 둘 다 가능하다.
     */
    record ProjectScoped(
        Long projectId,
        boolean includeOngoingMatchingRounds,
        boolean includeOngoingApplicationDetails
    ) implements ProjectApplicationAccessScope {

        public ProjectScoped(Long projectId) {
            this(projectId, false, false);
        }

        public ProjectScoped(Long projectId, boolean includeOngoing) {
            this(projectId, includeOngoing, includeOngoing);
        }
    }

    /**
     * 특정 지부들의 프로젝트 지원서 노출 (해당 기수 지부장).
     * <p>
     * 한 사용자가 동일 기수 내에서 여러 지부의 지부장을 맡는 경우를 지원하기 위해 List 로 받는다.
     */
    record ChapterScoped(List<Long> chapterIds, Long gisuId) implements ProjectApplicationAccessScope {
    }

    /**
     * 해당 기수의 모든 지원서 (총괄단 모니터링). SUPER_ADMIN 은 gisu 무관.
     */
    record AllInGisu(Long gisuId) implements ProjectApplicationAccessScope {
    }

    /**
     * 글로벌 (SUPER_ADMIN).
     */
    record All() implements ProjectApplicationAccessScope {
    }

    /**
     * 관리 대상 0건 (권한 없음).
     */
    record None() implements ProjectApplicationAccessScope {
    }
}
