package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectMember;

/**
 * ProjectMember 영속화 Port (Driven / Port Out).
 * <p>
 * - 신규 추가 / 상태 변경(soft delete 등): {@link #save}
 * - DRAFT/PENDING_REVIEW 단계 정정용 hard delete: {@link #hardDelete}
 */
public interface SaveProjectMemberPort {

    ProjectMember save(ProjectMember member);

    /**
     * 프로젝트 멤버 row 를 DB 에서 완전 삭제합니다.
     * <p>
     * IN_PROGRESS 이후 단계에서는 사용 금지 — 매칭/출석 등 외부 도메인 데이터에 영향. soft delete(상태 변경) 사용.
     */
    void hardDelete(Long projectMemberId);
}
