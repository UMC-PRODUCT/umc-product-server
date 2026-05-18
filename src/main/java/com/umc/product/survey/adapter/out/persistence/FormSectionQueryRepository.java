package com.umc.product.survey.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.QFormSection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FormSectionQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 폼에 속한 모든 섹션을 orderNo 오름차순으로 조회.
     */
    public List<FormSection> findAllByFormId(Long formId) {
        QFormSection s = QFormSection.formSection;
        return queryFactory
            .selectFrom(s)
            .where(s.form.id.eq(formId))
            .orderBy(s.orderNo.asc())
            .fetch();
    }
}
