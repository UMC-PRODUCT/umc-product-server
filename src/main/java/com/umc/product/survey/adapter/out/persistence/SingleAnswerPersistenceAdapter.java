package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadSingleAnswerPort;
import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.umc.product.survey.application.port.out.dto.VoteAnswerRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.umc.product.survey.domain.enums.QuestionType.CHECKBOX;

@Component
@RequiredArgsConstructor
public class SingleAnswerPersistenceAdapter implements SaveSingleAnswerPort, LoadSingleAnswerPort {

    private final SingleAnswerJpaRepository singleAnswerJpaRepository;
    private final SingleAnswerQueryRepository singleAnswerQueryRepository;

    @Override
    public void deleteAllByFormResponseIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        singleAnswerJpaRepository.deleteAllByFormResponseIds(ids);
    }

    @Override
    public Map<Long, Map<String, Object>> findScheduleValuesByFormResponseIds(List<Long> formResponseIds) {
        return singleAnswerQueryRepository.findScheduleValuesByFormResponseIds(formResponseIds);
    }

    @Override
    public Map<Long, Integer> countVotesByOptionId(Long formId) {
        List<Object[]> rows = singleAnswerJpaRepository.countVotesByOptionId(formId);

        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long optionId = ((Number) row[0]).longValue();
            Integer cnt = ((Number) row[1]).intValue();
            result.put(optionId, cnt);
        }
        return result;
    }

    @Override
    public Map<Long, List<Long>> findSelectedMemberIdsByOptionId(Long voteId) {
        List<VoteAnswerRow> rows = singleAnswerQueryRepository.findVoteAnswerRows(voteId);

        Map<Long, List<Long>> result = new LinkedHashMap<>();

        for (VoteAnswerRow row : rows) {
            Long memberId = row.respondentMemberId();
            Map<String, Object> value = row.value();

            if (value == null || value.isEmpty()) {
                continue;
            }

            switch (row.answeredAsType()) {
                case RADIO, DROPDOWN -> {
                    Long optionId = toLong(value.get("selectedOptionId"));
                    if (optionId != null) {
                        result.computeIfAbsent(optionId, k -> new ArrayList<>()).add(memberId);
                    }
                }
                case CHECKBOX -> {
                    Object raw = value.get("selectedOptionIds");
                    if (raw instanceof List<?> ids) {
                        for (Object id : ids) {
                            Long optionId = toLong(id);
                            if (optionId != null) {
                                result.computeIfAbsent(optionId, k -> new ArrayList<>()).add(memberId);
                            }
                        }
                    }
                }
                default -> {
                }
            }
        }

        return result;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s && !s.isBlank()) return Long.parseLong(s);
        return null;
    }
}
