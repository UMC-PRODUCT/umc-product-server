package com.umc.product.project.application.access;

import java.util.Objects;

import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.project.domain.Project;

/**
 * 지원서 합/불 판정 권한 규칙 (UPMS 권한 정리 '팀 매칭' 권한표).
 * <p>
 * 판정할 수 있는 사람은 다음 셋이다. 지부장·중앙운영진·보조 PM 은 판정하지 못한다.
 * <ul>
 *   <li>프로젝트의 담당 PM(PO)</li>
 *   <li>프로젝트 기수의 총괄·부총괄</li>
 *   <li>최고관리자(SUPER_ADMIN)</li>
 * </ul>
 * 판정 가능 시간(지원 마감 후 ~ 결정 마감)은 도메인({@code ProjectMatchingRound.validateIsMutableAt})이 책임진다.
 * 판정 API 의 권한 평가기와 프론트용 "판정 가능" 플래그가 모두 이 규칙을 쓴다.
 */
public final class ProjectApplicationDecisionPolicy {

    private ProjectApplicationDecisionPolicy() {
    }

    public static boolean canDecide(SubjectAttributes subject, Project project) {
        return Objects.equals(subject.memberId(), project.getProductOwnerMemberId())
            || subject.toAuthoritySnapshot().isCentralCoreInGisu(project.getGisuId());
    }
}
