package com.umc.product.survey.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.QFormSection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    /**
     * 여러 폼에 속한 모든 섹션을 한 번에 조회한다.
     */
    public List<FormSection> findAllByFormIdIn(Collection<Long> formIds) {
        if (formIds == null || formIds.isEmpty()) {
            return List.of();
        }

        QFormSection s = QFormSection.formSection;
        return queryFactory
            .selectFrom(s)
            .join(s.form).fetchJoin()
            .where(s.form.id.in(formIds))
            .orderBy(s.form.id.asc(), s.orderNo.asc())
            .fetch();
    }
}
