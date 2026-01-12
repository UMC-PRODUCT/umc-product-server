package com.umc.product.challenger.application.port.in.query.dto;


import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerMission;
import com.umc.product.challenger.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;

public record ChallengerWorkbookSummary(
        Long challengerWorkbookId,
        Long challengerId,
        String challengerName,
        String schoolName,
        String part,
        String workbookTitle,
        String submission,
        WorkbookStatus status,
        Boolean isBest
) {
    public static ChallengerWorkbookSummary from(
            ChallengerWorkbook workbook,
            ChallengerMission mission,
            Challenger challenger,
//           TODO: 나중에 추가
//            Member member,
//            School school,
            OriginalWorkbook originalWorkbook
    ) {
        return new ChallengerWorkbookSummary(
                workbook.getId(),
                challenger.getId(),
                null,  // TODO: member.getName()
                null,  // TODO: school.getName()
                challenger.getPart().name(),
                originalWorkbook.getTitle(),
                mission != null ? mission.getSubmission() : null,
                workbook.getStatus(),
                workbook.getIsBest()
        );
    }

}
