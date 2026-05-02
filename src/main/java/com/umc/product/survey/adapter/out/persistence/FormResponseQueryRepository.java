package com.umc.product.survey.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.QFormResponse;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FormResponseQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 폼의 모든 응답을 id 내림차순으로 조회 (DRAFT + SUBMITTED 모두 포함).
     */
    public List<FormResponse> findAllByFormId(Long formId) {
        QFormResponse fr = QFormResponse.formResponse;
        return queryFactory
            .selectFrom(fr)
            .where(fr.form.id.eq(formId))
            .orderBy(fr.id.desc())
            .fetch();
    }

    /**
     * 폼의 SUBMITTED 응답 목록을 id 내림차순으로 조회.
     */
    public List<FormResponse> findAllSubmittedByFormId(Long formId) {
        QFormResponse fr = QFormResponse.formResponse;
        return queryFactory
            .selectFrom(fr)
            .where(
                fr.form.id.eq(formId),
                fr.status.eq(FormResponseStatus.SUBMITTED)
            )
            .orderBy(fr.id.desc())
            .fetch();
    }
}
