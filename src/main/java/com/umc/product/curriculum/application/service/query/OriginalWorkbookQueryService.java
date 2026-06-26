package com.umc.product.curriculum.application.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.curriculum.application.port.in.query.GetOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.OriginalWorkbookInfo;
import com.umc.product.curriculum.application.port.in.query.dto.OriginalWorkbookInfo.OriginalWorkbookMissionInfo;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookMissionPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.OriginalWorkbookMission;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OriginalWorkbookQueryService implements GetOriginalWorkbookUseCase {

    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final LoadOriginalWorkbookMissionPort loadOriginalWorkbookMissionPort;

    @Override
    public OriginalWorkbookInfo getById(Long originalWorkbookId) {
        OriginalWorkbook workbook = loadOriginalWorkbookPort.getById(originalWorkbookId);
        List<OriginalWorkbookMission> missions =
            loadOriginalWorkbookMissionPort.findByOriginalWorkbookId(originalWorkbookId);

        return OriginalWorkbookInfo.builder()
            .originalWorkbookId(workbook.getId())
            .title(workbook.getTitle())
            .description(workbook.getDescription())
            .url(workbook.getUrl())
            .content(workbook.getContent())
            .type(workbook.getType())
            .status(workbook.getOriginalWorkbookStatus())
            .releasedAt(workbook.getReleasedAt())
            .releasedMemberId(workbook.getReleasedMemberId())
            .missions(missions.stream()
                .map(this::toMissionInfo)
                .toList())
            .build();
    }

    private OriginalWorkbookMissionInfo toMissionInfo(OriginalWorkbookMission mission) {
        return OriginalWorkbookMissionInfo.builder()
            .originalWorkbookMissionId(mission.getId())
            .title(mission.getTitle())
            .description(mission.getDescription())
            .missionType(mission.getMissionType())
            .isNecessary(mission.isNecessary())
            .build();
    }
}
