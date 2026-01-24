package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.domain.enums.FormSectionType;
import java.util.Comparator;
import java.util.List;

public record RecruitmentFormDefinitionInfo(
        List<RecruitmentSectionInfo> sections
) {
    public record RecruitmentSectionInfo(
            SectionKind kind,        // COMMON_PAGE / PART
            Integer pageNo,          // COMMON_PAGE
            ChallengerPart part,     // PART
            Integer orderNo,
            List<FormDefinitionInfo.QuestionInfo> questions
    ) {
        public enum SectionKind {COMMON_PAGE, PART}
    }

    public static RecruitmentFormDefinitionInfo from(FormDefinitionInfo def) {
        if (def == null || def.sections() == null) {
            return new RecruitmentFormDefinitionInfo(List.of());
        }

        List<RecruitmentSectionInfo> sections = def.sections().stream()
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(
                        FormDefinitionInfo.FormSectionInfo::orderNo,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(RecruitmentFormDefinitionInfo::fromSection)
                .filter(java.util.Objects::nonNull)
                .toList();

        return new RecruitmentFormDefinitionInfo(sections);
    }

    private static RecruitmentSectionInfo fromSection(FormDefinitionInfo.FormSectionInfo sec) {
        List<FormDefinitionInfo.QuestionInfo> qs =
                (sec.questions() == null ? List.<FormDefinitionInfo.QuestionInfo>of() : sec.questions()).stream()
                        .filter(java.util.Objects::nonNull)
                        .sorted(Comparator.comparing(
                                FormDefinitionInfo.QuestionInfo::orderNo,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .toList();

        if (sec.type() == FormSectionType.CUSTOM) {
            ChallengerPart part = parsePart(sec.targetKey()); // targetKey -> part
            return new RecruitmentSectionInfo(
                    RecruitmentSectionInfo.SectionKind.PART,
                    null,
                    part,
                    sec.orderNo(),
                    qs
            );
        }

        Integer pageNo = sec.orderNo(); // pageNo == orderNo 정책
        return new RecruitmentSectionInfo(
                RecruitmentSectionInfo.SectionKind.COMMON_PAGE,
                pageNo,
                null,
                sec.orderNo(),
                qs
        );
    }

    private static ChallengerPart parsePart(String targetKey) {
        if (targetKey == null || targetKey.isBlank()) {
            return null;
        }
        try {
            return ChallengerPart.valueOf(targetKey);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
