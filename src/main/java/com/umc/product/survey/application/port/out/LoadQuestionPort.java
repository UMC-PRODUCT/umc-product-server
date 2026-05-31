package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadQuestionPort {
    List<Question> findAllByFormSectionIdIn(Set<Long> formSectionIds);

    boolean existsByIdAndFormId(Long questionId, Long formId);

    Optional<Question> findById(Long questionId);

    Optional<Question> findFirstByFormIdAndType(Long formId, QuestionType type);

    /**
     * 특정 폼에 속한 모든 질문을 조회합니다.
     * 필수 질문 누락 검증 등에 사용.
     */
    List<Question> listByFormId(Long formId);

    /**
     * 특정 섹션에 속한 모든 질문을 orderNo 오름차순으로 조회.
     */
    List<Question> listBySectionId(Long sectionId);

    /**
     * 여러 섹션의 모든 질문을 한 번에 조회 (벌크). orderNo 오름차순 정렬.
     * 폼 전체 구조 조회 등 N+1 회피 용도.
     */
    List<Question> listBySectionIdIn(Set<Long> sectionIds);
}
