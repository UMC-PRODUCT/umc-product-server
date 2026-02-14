package com.umc.product.recruitment.adapter.out;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.recruitment.domain.QApplication.application;
import static com.umc.product.recruitment.domain.QApplicationPartPreference.applicationPartPreference;
import static com.umc.product.recruitment.domain.QInterviewAssignment.interviewAssignment;
import static com.umc.product.recruitment.domain.QInterviewSlot.interviewSlot;
import static com.umc.product.recruitment.domain.QRecruitmentPart.recruitmentPart;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAssignmentRow;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.QApplicationPartPreference;
import com.umc.product.recruitment.domain.QRecruitmentPart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterviewAssignmentQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 Recruitment의 모든 InterviewAssignment를 Slot, Application과 함께 조회 slot.startsAt 기준 오름차순 정렬
     */
    public List<InterviewAssignment> findByRecruitmentIdWithSlotAndApplication(Long recruitmentId) {
        return queryFactory
            .selectFrom(interviewAssignment)
            .join(interviewAssignment.slot, interviewSlot).fetchJoin()
            .join(interviewAssignment.application, application).fetchJoin()
            .where(interviewAssignment.recruitment.id.eq(recruitmentId))
            .orderBy(interviewSlot.startsAt.asc())
            .fetch();
    }

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

    public Set<Long> findAssignedApplicationIdsByRecruitmentId(Long recruitmentId) {
        return queryFactory
            .select(interviewAssignment.application.id)
            .from(interviewAssignment)
            .where(interviewAssignment.recruitment.id.eq(recruitmentId))
            .fetch()
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    public List<InterviewSchedulingAssignmentRow> findAssignmentRowsByRecruitmentIdAndSlotId(
        Long recruitmentId,
        Long slotId,
        PartOption requestedPart
    ) {
        QApplicationPartPreference pref1 = new QApplicationPartPreference("pref1");
        QApplicationPartPreference pref2 = new QApplicationPartPreference("pref2");
        QRecruitmentPart rp1 = new QRecruitmentPart("rp1");
        QRecruitmentPart rp2 = new QRecruitmentPart("rp2");

        ChallengerPart filterPart = null;
        if (requestedPart != null && requestedPart != PartOption.ALL) {
            filterPart = ChallengerPart.valueOf(requestedPart.name());
        }

        return queryFactory
            .select(Projections.constructor(
                InterviewSchedulingAssignmentRow.class,
                interviewAssignment.id,
                interviewAssignment.application.id,
                member.nickname,
                member.name,
                rp1.part,
                rp2.part
            ))
            .from(interviewAssignment)
            .join(interviewAssignment.application, application)
            .join(member).on(member.id.eq(application.applicantMemberId))

            // 1지망
            .leftJoin(pref1).on(
                pref1.application.eq(application),
                pref1.priority.eq(1)
            )
            .leftJoin(pref1.recruitmentPart, rp1)

            // 2지망
            .leftJoin(pref2).on(
                pref2.application.eq(application),
                pref2.priority.eq(2)
            )
            .leftJoin(pref2.recruitmentPart, rp2)

            .where(
                interviewAssignment.recruitment.id.eq(recruitmentId),
                interviewAssignment.slot.id.eq(slotId),
                ObjectUtils.isEmpty(filterPart) ? null : rp1.part.eq(filterPart)
            )
            .orderBy(interviewAssignment.createdAt.asc())
            .fetch();
    }

    public boolean existsByApplicationId(Long applicationId) {
        Integer result = queryFactory
            .selectOne()
            .from(interviewAssignment)
            .where(interviewAssignment.application.id.eq(applicationId))
            .fetchFirst();

        return result != null;
    }

}
