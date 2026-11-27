package com.umc.product.inquiry.domain;

import java.util.Objects;

import com.umc.product.common.BaseEntity;
import com.umc.product.inquiry.domain.enums.InquiryHistoryType;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inquiry_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InquiryHistoryType type;

    @Column(name = "actor_member_id", nullable = false)
    private Long actorMemberId;

    @Column(name = "previous_assignee_member_id")
    private Long previousAssigneeMemberId;

    @Column(name = "new_assignee_member_id")
    private Long newAssigneeMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_team", length = 40)
    private InquiryTeam previousTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_team", length = 40)
    private InquiryTeam newTeam;

    static InquiryHistory assigneeChanged(Long inquiryId, Long actorMemberId, Long previous, Long next) {
        InquiryHistory history = of(inquiryId, actorMemberId, InquiryHistoryType.ofAssigneeChange(previous, next));
        history.previousAssigneeMemberId = previous;
        history.newAssigneeMemberId = next;
        return history;
    }

    static InquiryHistory teamChanged(Long inquiryId, Long actorMemberId, InquiryTeam previous, InquiryTeam next) {
        InquiryHistory history = of(inquiryId, actorMemberId, InquiryHistoryType.TEAM_CHANGED);
        history.previousTeam = previous;
        history.newTeam = next;
        return history;
    }

    public boolean notifiesNewAssignee() {
        return newAssigneeMemberId != null && !newAssigneeMemberId.equals(actorMemberId);
    }

    private static InquiryHistory of(Long inquiryId, Long actorMemberId, InquiryHistoryType type) {
        InquiryHistory history = new InquiryHistory();
        history.inquiryId = Objects.requireNonNull(inquiryId);
        history.actorMemberId = Objects.requireNonNull(actorMemberId);
        history.type = type;
        return history;
    }
}
