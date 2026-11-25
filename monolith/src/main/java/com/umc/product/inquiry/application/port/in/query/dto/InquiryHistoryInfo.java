package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;
import java.util.function.Function;

import com.umc.product.inquiry.domain.InquiryHistory;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

/**
 * 담당자 변경은 {@code previousAssignee}·{@code newAssignee}를, 팀 변경은 {@code previousTeam}·{@code newTeam}을 채운다.
 * 문의 접수({@link Kind#RECEIVED})는 저장된 이력이 아니라 문의 접수 시각으로 만든 첫 줄이고, 작성자가 한 일로 둔다.
 */
public record InquiryHistoryInfo(
    Kind type,
    InquiryStaffMemberInfo actor,
    InquiryStaffMemberInfo previousAssignee,
    InquiryStaffMemberInfo newAssignee,
    InquiryTeam previousTeam,
    InquiryTeam newTeam,
    Instant occurredAt
) {

    public static InquiryHistoryInfo received(InquiryStaffMemberInfo author, Instant receivedAt) {
        return new InquiryHistoryInfo(Kind.RECEIVED, author, null, null, null, null, receivedAt);
    }

    public static InquiryHistoryInfo from(InquiryHistory history, Function<Long, InquiryStaffMemberInfo> member) {
        return new InquiryHistoryInfo(
            Kind.valueOf(history.getType().name()),
            member.apply(history.getActorMemberId()),
            member.apply(history.getPreviousAssigneeMemberId()),
            member.apply(history.getNewAssigneeMemberId()),
            history.getPreviousTeam(),
            history.getNewTeam(),
            history.getCreatedAt()
        );
    }

    /** 저장하는 {@link com.umc.product.inquiry.domain.enums.InquiryHistoryType}에 접수 줄을 더한 응답용 종류. */
    public enum Kind {
        RECEIVED,
        ASSIGNED,
        REASSIGNED,
        UNASSIGNED,
        TEAM_CHANGED
    }
}
