package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Question;

public interface SaveQuestionPort {

    Question save(Question question);

    void deleteById(Long questionId);

    void deleteByFormIdAndQuestionId(Long formId, Long questionId);

    /**
     * 특정 폼에 속한 모든 질문 삭제 (deleteForm cascade 용)
     */
    void deleteByFormId(Long formId);

    /**
     * 특정 섹션에 속한 모든 질문 삭제 (deleteSection cascade 용)
     */
    void deleteBySectionId(Long sectionId);
}
