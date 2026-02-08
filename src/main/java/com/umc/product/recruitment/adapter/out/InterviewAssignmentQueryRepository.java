package com.umc.product.recruitment.adapter.out;

import static com.umc.product.recruitment.domain.QApplication.application;
import static com.umc.product.recruitment.domain.QApplicationPartPreference.applicationPartPreference;
import static com.umc.product.recruitment.domain.QInterviewAssignment.interviewAssignment;
import static com.umc.product.recruitment.domain.QInterviewSlot.interviewSlot;
import static com.umc.product.recruitment.domain.QRecruitmentPart.recruitmentPart;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterviewAssignmentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public long countByRecruitmentId(Long recruitmentId) {
        return queryFactory
            .select(interviewAssignment.count())
            .from(interviewAssignment)
            .where(interviewAssignment.recruitment.id.eq(recruitmentId))
            .fetchOne();
    }

    public long countByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, ChallengerPart part) {
        return queryFactory
            .select(interviewAssignment.count())
            .from(interviewAssignment)
            .join(interviewAssignment.application, application)
            .join(applicationPartPreference).on(
                applicationPartPreference.application.eq(application),
                applicationPartPreference.priority.eq(1)
            )
            .join(applicationPartPreference.recruitmentPart, recruitmentPart)
            .where(
                interviewAssignment.recruitment.id.eq(recruitmentId),
                recruitmentPart.part.eq(part)
            )
            .fetchOne();
    }

    public long countByRecruitmentIdAndDateAndFirstPreferredPart(
        Long recruitmentId,
        LocalDate date,
        ChallengerPart part
    ) {
        Instant start = date.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();

        return queryFactory
            .select(interviewAssignment.count())
            .from(interviewAssignment)
            .join(interviewAssignment.slot, interviewSlot)
            .join(interviewAssignment.application, application)
            .join(applicationPartPreference).on(
                applicationPartPreference.application.eq(application),
                applicationPartPreference.priority.eq(1)
            )
            .join(applicationPartPreference.recruitmentPart, recruitmentPart)
            .where(
                interviewAssignment.recruitment.id.eq(recruitmentId),
                recruitmentPart.part.eq(part),
                interviewSlot.startsAt.goe(start),
                interviewSlot.startsAt.lt(end)
            )
            .fetchOne();
    }
}
