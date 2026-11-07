package com.umc.product.project.application.access;

import java.time.Instant;

import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;

/** 지원자 목록에서는 PM 결정 기간에 해당 차수의 합불 결과를 판정 권한자에게만 노출한다. */
public final class ProjectApplicationResultVisibilityPolicy {

    private ProjectApplicationResultVisibilityPolicy() {
    }

    public static boolean canViewDecisionResult(
        SubjectAttributes subject,
        ProjectApplication application,
        Instant now
    ) {
        ProjectMatchingRound round = application.getAppliedMatchingRound();
        boolean decisionPeriod = now.isAfter(round.getEndsAt()) && !round.isDecisionDeadlinePassed(now);
        return !decisionPeriod || ProjectApplicationDecisionPolicy.canDecide(
            subject, application.getApplicationForm().getProject());
    }
}
