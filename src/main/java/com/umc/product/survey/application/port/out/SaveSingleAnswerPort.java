package com.umc.product.survey.application.port.out;

import java.util.List;

public interface SaveSingleAnswerPort {

//    void saveAll(List<SingleAnswer> answers);
//
//    void upsert(Long formResponseId, SingleAnswer answer);
//
//    void upsertAll(Long formResponseId, List<SingleAnswer> answers);

    void deleteAllByFormResponseIds(List<Long> formResponseIds);
}
