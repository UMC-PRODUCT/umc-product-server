package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "form_response")
public class FormResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(name = "respondent_member_id", nullable = false)
    private Long respondentMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FormResponseStatus status = FormResponseStatus.DRAFT;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "submitted_ip")
    private String submittedIp;

    @Column(name = "last_saved_at", nullable = false)
    private Instant lastSavedAt;

    @OneToMany(mappedBy = "formResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SingleAnswer> answers = new ArrayList<>();

    public static FormResponse createDraft(Form form, Long respondentMemberId) {
        FormResponse fr = new FormResponse();
        fr.form = form;
        fr.respondentMemberId = respondentMemberId;
        fr.status = com.umc.product.survey.domain.enums.FormResponseStatus.DRAFT;
        fr.lastSavedAt = Instant.now();
        return fr;
    }

    public void submit(java.time.Instant submittedAt, String submittedIp) {
        if (this.status == com.umc.product.survey.domain.enums.FormResponseStatus.SUBMITTED) {
            return;
        }
        this.status = com.umc.product.survey.domain.enums.FormResponseStatus.SUBMITTED;
        this.submittedAt = submittedAt;
        this.submittedIp = submittedIp;
    }

    public void updateLastSavedAt(Instant now) {
        this.lastSavedAt = now;
    }

    public static FormResponse createVoteResponse(
        Form form,
        Long respondentMemberId,
        Question question,
        List<Long> selectedOptionIds,
        Instant now
    ) {
        FormResponse fr = new FormResponse();
        fr.form = form;
        fr.respondentMemberId = respondentMemberId;

        // 투표는 즉시 제출로 처리
        fr.status = FormResponseStatus.SUBMITTED;
        fr.submittedAt = now;

        fr.lastSavedAt = now;

        // answers 구성 (SingleAnswer 구조에 맞게 구현 필요)
        // 예: 객관식(옵션 선택) 1문항에 대해 optionIds를 담는 형태
        fr.answers = new ArrayList<>();
        fr.answers.add(SingleAnswer.createVoteAnswer(fr, question, selectedOptionIds));

        return fr;
    }

}
