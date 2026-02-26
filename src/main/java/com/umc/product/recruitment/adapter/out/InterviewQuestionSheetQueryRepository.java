package com.umc.product.recruitment.adapter.out;

import static com.umc.product.recruitment.domain.QInterviewQuestionSheet.interviewQuestionSheet;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterviewQuestionSheetQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 여러 PartKey에 해당하는 질문들을 조회 (partKey, orderNo 순 정렬)
     */
    public List<InterviewQuestionSheet> findByRecruitmentIdAndPartKeysOrderByOrderNoAsc(
        Long recruitmentId, Set<PartKey> partKeys
    ) {
        return queryFactory
            .selectFrom(interviewQuestionSheet)
            .where(
                interviewQuestionSheet.recruitment.id.eq(recruitmentId),
                interviewQuestionSheet.partKey.in(partKeys)
            )
            .orderBy(interviewQuestionSheet.partKey.asc(), interviewQuestionSheet.orderNo.asc())
            .fetch();
    }
}
