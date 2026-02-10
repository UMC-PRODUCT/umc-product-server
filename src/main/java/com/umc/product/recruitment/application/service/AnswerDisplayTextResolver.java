package com.umc.product.recruitment.application.service;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationDetailInfo;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AnswerDisplayTextResolver {

    public String resolve(
        QuestionType questionType,
        List<ApplicationDetailInfo.OptionInfo> options,
        Object rawValue
    ) {
        if (questionType == null || rawValue == null) {
            return null;
        }
        if (!(rawValue instanceof Map<?, ?> m)) {
            return String.valueOf(rawValue);
        }

        return switch (questionType) {
            case SHORT_TEXT, LONG_TEXT -> asString(m.get("text"));

            case RADIO, DROPDOWN -> {
                String selectedId = asString(m.get("selectedOptionId"));
                String otherText = asString(m.get("otherText"));
                String base = findOptionContent(options, selectedId);
                yield withOther(base, otherText);
            }

            case CHECKBOX -> {
                List<String> ids = asStringList(m.get("selectedOptionIds"));
                String otherText = asString(m.get("otherText"));

                String base = ids.stream()
                    .map(id -> findOptionContent(options, id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));

                yield withOther(base, otherText);
            }

            case PREFERRED_PART -> {
                List<String> parts = asStringList(m.get("preferredParts"));
                yield parts.isEmpty() ? null : String.join(", ", parts);
            }

            case SCHEDULE -> null;

            case PORTFOLIO -> {
                int fileCount = (m.get("files") instanceof List<?> l) ? l.size() : 0;
                int linkCount = (m.get("links") instanceof List<?> l) ? l.size() : 0;
                if (fileCount == 0 && linkCount == 0) {
                    yield null;
                }
                yield "파일 " + fileCount + "개 / 링크 " + linkCount + "개";
            }

            default -> null;
        };
    }

    private String findOptionContent(List<ApplicationDetailInfo.OptionInfo> options, String selectedId) {
        if (selectedId == null || options == null) {
            return null;
        }
        for (var o : options) {
            if (o == null) {
                continue;
            }
            // optionId(Long) vs selectedId("11") 혼합 대응
            if (selectedId.equals(String.valueOf(o.optionId()))) {
                return o.content();
            }
        }
        return null;
    }

    private String withOther(String base, String otherText) {
        if (otherText == null || otherText.isBlank()) {
            return base;
        }
        if (base == null || base.isBlank()) {
            return otherText;
        }
        return base + " (기타: " + otherText + ")";
    }

    private String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private List<String> asStringList(Object v) {
        if (!(v instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(Objects::nonNull).map(String::valueOf).toList();
    }
}
