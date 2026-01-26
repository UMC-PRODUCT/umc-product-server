package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.domain.enums.FormSectionType;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record RecruitmentFormDefinitionInfo(
        List<RecruitmentSectionInfo> sections
) {
    public record RecruitmentSectionInfo(
            Long sectionId, // survey 도메인의 question을 찾아가기 위한 key: FormSection 식별자
            SectionKind kind,        // COMMON_PAGE / PART
            Integer pageNo,          // COMMON_PAGE
            ChallengerPart part,     // PART
            Integer orderNo
            // List<FormDefinitionInfo.QuestionInfo> questions
    ) {
        public enum SectionKind {COMMON_PAGE, PART}
    }

    public static RecruitmentFormDefinitionInfo from(FormDefinitionInfo def) {
        if (def == null || def.sections() == null) {
            return new RecruitmentFormDefinitionInfo(List.of());
        }

        List<RecruitmentSectionInfo> sections = def.sections().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        FormDefinitionInfo.FormSectionInfo::orderNo,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(RecruitmentFormDefinitionInfo::toRecruitmentSection)
                .filter(Objects::nonNull)
                .toList();

        return new RecruitmentFormDefinitionInfo(sections);
    }

    private static RecruitmentSectionInfo toRecruitmentSection(FormDefinitionInfo.FormSectionInfo sec) {
        Long sectionId = sec.sectionId();

        if (sectionId == null) {
            return null;
        }

        Integer orderNo = sec.orderNo();

        // 파트별 질문
        if (sec.type() == FormSectionType.CUSTOM) {
            ChallengerPart part = parsePart(sec.targetKey()); // targetKey -> part
            return new RecruitmentSectionInfo(
                    sectionId,
                    RecruitmentSectionInfo.SectionKind.PART,
                    null,
                    part,
                    orderNo
            );
        }

        // 공통 질문
        Integer pageNo = sec.orderNo(); // pageNo == orderNo 정책
        return new RecruitmentSectionInfo(
                sectionId,
                RecruitmentSectionInfo.SectionKind.COMMON_PAGE,
                pageNo,
                null,
                orderNo
        );
    }

    private static ChallengerPart parsePart(String targetKey) {
        if (targetKey == null || targetKey.isBlank()) {
            return null;
        }
        if (targetKey.startsWith("PART:")) {
            targetKey = targetKey.substring("PART:".length());
        }
        try {
            return ChallengerPart.valueOf(targetKey);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
