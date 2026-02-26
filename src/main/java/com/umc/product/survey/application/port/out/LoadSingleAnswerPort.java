package com.umc.product.survey.application.port.out;

import java.util.List;
import java.util.Map;

public interface LoadSingleAnswerPort {
    //List<SingleAnswer> findByFormResponseId(Long formResponseId);

    Map<Long, Map<String, Object>> findScheduleValuesByFormResponseIds(List<Long> formResponseIds);

    Map<Long, Integer> countVotesByOptionId(Long formId);
    
}
