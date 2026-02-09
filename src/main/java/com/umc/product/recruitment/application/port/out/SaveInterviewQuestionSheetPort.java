package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewQuestionSheet;

public interface SaveInterviewQuestionSheetPort {

    InterviewQuestionSheet save(InterviewQuestionSheet interviewQuestionSheet);

    void deleteById(Long interviewQuestionSheetId);
}
