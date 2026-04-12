package com.umc.product.project.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.domain.FormResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지원자가 제출한 지원서의 실제 내용을 담고 있는 {@link FormResponse}과 매핑해주는 엔티티입니다.
 * <p>
 * 어떤 매칭 라운드에 지원한 폼인지와 지원 결과를 담고 있습니다.
 */
@Entity
@Table(
    name = "project_application",
    uniqueConstraints = {
        // 각 지원자는 매칭 차수 당 한 개의 지원서만 제출할 수 있습니다.
        @UniqueConstraint(
            name = "uk_project_application_form_member_matching_round",
            columnNames = {"project_application_form_id", "applied_matching_round_id", "applicantMemberId"}
        )
    }
)
// 각 지원자는 매칭 차수 당 한 개의 지원서만 제출할 수 있습니다.
// UK로 관리하려 했으나, CANCELLED를 이용해서 Soft Delete 시키도록 설계를 변경하여 Service 단에서 검증을 진행해야 합니다.
// TODO: 이거 반드시 해야함!!!!
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_application_form_id", nullable = false)
    private ProjectApplicationForm applicationForm; // 어떤 지원용 폼에 접수된 지원서인지

    @Column(nullable = false)
    private Long formResponseId; // 응답한 지원서 내용

    @Column(nullable = false)
    private Long applicantMemberId; // 지원자 멤버 ID

    // 참고: PostgreSQL은 null일 때 Unique 제약을 풀어주기에 중복이 없음을 보장할 수 없게 됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_matching_round_id", nullable = false)
    // 어떤 매칭 라운드에 지원한 지원서인지 나타내며, 반드시 존재하여야 합니다.
    // 추후 UMC 내에서 프로젝트 멤버를 더 구할 수 있도록 하는 경우, 별도의 엔티티를 통하여 기능을 제공하여야 할 것으로 보임.
    private ProjectMatchingRound appliedMatchingRound;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectApplicationStatus status;
    private Long statusChangedMemberId;
    private String statusChangeReason;

    @Builder(access = AccessLevel.PRIVATE)
    private ProjectApplication(
        ProjectApplicationForm applicationForm, Long formResponseId,
        Long applicantMemberId, ProjectMatchingRound appliedMatchingRound
    ) {
        this.applicationForm = applicationForm;
        this.formResponseId = formResponseId;
        this.applicantMemberId = applicantMemberId;
        this.appliedMatchingRound = appliedMatchingRound;
        this.status = ProjectApplicationStatus.PENDING;
    }

    public static ProjectApplication create(
        ProjectApplicationForm form, Long formResponseId,
        Long applicantMemberId, ProjectMatchingRound appliedMatchingRound
    ) {
        return ProjectApplication.builder()
            .applicationForm(form)
            .formResponseId(formResponseId)
            .applicantMemberId(applicantMemberId)
            .appliedMatchingRound(appliedMatchingRound)
            .build();
    }

    /**
     * 지원서를 합격시킬 때 사용합니다.
     *
     * @param decidedByMemberId 결정한 PO 또는 운영진 ID
     * @param reason            결정 사유 (필수 아님)
     */
    public void approve(Long decidedByMemberId, String reason) {
        validateIsSubmitted("지원서가 제출된 상태에서만 합격 처리할 수 있습니다.");

        this.status = ProjectApplicationStatus.APPROVED;
        this.statusChangedMemberId = decidedByMemberId;
        this.statusChangeReason = reason;
    }

    /**
     * 지원서를 불합격시킬 때 사용합니다.
     *
     * @param decidedByMemberId 결정한 PO 또는 운영진 ID
     * @param reason            결정 사유 (필수 아님)
     */
    public void reject(Long decidedByMemberId, String reason) {
        validateIsSubmitted("지원서가 제출된 상태에서만 불합격 처리할 수 있습니다.");

        this.status = ProjectApplicationStatus.REJECTED;
        this.statusChangedMemberId = decidedByMemberId;
        this.statusChangeReason = reason;
    }

    /**
     * 지원 취소 (철회)
     *
     * @param decidedByMemberId 실행자 ID (지원자 본인 또는 운영진)
     * @param reason            취소 사유 (필수 아님)
     */
    public void cancel(Long decidedByMemberId, String reason) {
//        validateIsSubmitted("지원서가 제출된 상태에서만 철회할 수 있습니다.");

        // HARD DELETE 로직
        // PENDING일 떄, 즉 임시저장본 일 때 삭제하는 로직 또한 필요할 것 같음

        // TODO: 악악악...
    }

    /**
     * 지원서 제출 처리 (임시저장에서만 이동 가능)
     */
    public void submit() {
        if (!this.isPending()) {
            throw new ProjectDomainException(ProjectErrorCode.APPLICATION_NOT_SUBMITTED);
        }

        this.status = ProjectApplicationStatus.SUBMITTED;
    }

    public boolean isPending() {
        return this.status == ProjectApplicationStatus.PENDING;
    }

    public boolean isSubmitted() {
        return this.status == ProjectApplicationStatus.SUBMITTED;
    }

    public void validateIsSubmitted(String message) {
        if (isSubmitted()) {
            return;
        }

        throw new ProjectDomainException(ProjectErrorCode.APPLICATION_NOT_SUBMITTED, message);
    }
}
