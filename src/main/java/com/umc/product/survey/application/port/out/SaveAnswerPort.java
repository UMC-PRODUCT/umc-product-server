package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Answer;

import java.util.List;

public interface SaveAnswerPort {

    List<Answer> saveAll(List<Answer> answers);

    /**
     * 특정 FormResponse에 속한 모든 Answer(및 연쇄 AnswerChoice)를 삭제.
     * updateResponse 시 기존 답변을 지우고 새로 쓰기 위해 사용.
     */
    void deleteAllByFormResponseId(Long formResponseId);
}
