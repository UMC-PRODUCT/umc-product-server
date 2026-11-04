package com.umc.product.test.domain.startingline;

import java.util.Optional;

import com.umc.product.project.domain.enums.ProjectStatus;

/**
 * 출발선 프로젝트 하나가 거치는 단계. 앞 네 단계는 PO, 뒤 두 단계는 superadmin이 실행한다.
 */
public enum StartingLineStep {
    CREATE_DRAFT,
    UPDATE_INFO,
    UPSERT_FORM,
    SUBMIT,
    SET_QUOTAS,
    PUBLISH;

    /**
     * 지금 상태에서 목표까지 다음에 실행할 단계. 목표에 닿았거나 넘었으면 비어 있다.
     *
     * @param current 이 기수에 PO 프로젝트가 없으면 null
     * @param quota   null이면 QA 프로젝트(제출까지만), 아니면 이 TO를 넣고 공개한다
     */
    public static Optional<StartingLineStep> next(OwnedProject current, ProjectQuota quota) {
        if (current == null) {
            return Optional.of(CREATE_DRAFT);
        }
        if (current.status() == ProjectStatus.DRAFT) {
            if (!current.hasName()) {
                return Optional.of(UPDATE_INFO);
            }
            return Optional.of(current.hasForm() ? SUBMIT : UPSERT_FORM);
        }
        if (current.status() != ProjectStatus.PENDING_REVIEW || quota == null) {
            return Optional.empty();
        }
        return Optional.of(current.quotas().equals(quota.entries()) ? PUBLISH : SET_QUOTAS);
    }
}
