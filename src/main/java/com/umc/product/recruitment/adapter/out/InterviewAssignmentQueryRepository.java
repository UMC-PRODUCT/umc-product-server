package com.umc.product.recruitment.adapter.out;

import static com.umc.product.recruitment.domain.QApplication.application;
import static com.umc.product.recruitment.domain.QInterviewAssignment.interviewAssignment;
import static com.umc.product.recruitment.domain.QInterviewSlot.interviewSlot;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.recruitment.domain.InterviewAssignment;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
}
