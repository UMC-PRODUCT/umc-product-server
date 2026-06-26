package com.umc.product.curriculum.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.OriginalWorkbookInfo;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookMissionPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.OriginalWorkbookMission;
import com.umc.product.curriculum.domain.WeeklyCurriculum;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;

@ExtendWith(MockitoExtension.class)
class OriginalWorkbookQueryServiceTest {

    @Mock
    LoadOriginalWorkbookPort loadOriginalWorkbookPort;

    @Mock
    LoadOriginalWorkbookMissionPort loadOriginalWorkbookMissionPort;

    @InjectMocks
    OriginalWorkbookQueryService sut;

    @Test
    @DisplayName("원본 워크북 상세 조회 시 워크북과 미션 목록을 조립한다")
    void getOriginalWorkbookSuccess() {
        // given
        OriginalWorkbook workbook = originalWorkbook();
        OriginalWorkbookMission mission = OriginalWorkbookMission.create(
            workbook, "미션 1", "링크 제출", MissionType.LINK, true
        );
        ReflectionTestUtils.setField(mission, "id", 300L);

        given(loadOriginalWorkbookPort.getById(200L)).willReturn(workbook);
        given(loadOriginalWorkbookMissionPort.findByOriginalWorkbookId(200L)).willReturn(List.of(mission));

        // when
        OriginalWorkbookInfo result = sut.getById(200L);

        // then
        assertThat(result.originalWorkbookId()).isEqualTo(200L);
        assertThat(result.title()).isEqualTo("1주차 워크북");
        assertThat(result.description()).isEqualTo("상세 설명");
        assertThat(result.url()).isEqualTo("https://workbook.example.com");
        assertThat(result.content()).isEqualTo("워크북 본문");
        assertThat(result.type()).isEqualTo(OriginalWorkbookType.MAIN);
        assertThat(result.status()).isEqualTo(OriginalWorkbookStatus.RELEASED);
        assertThat(result.releasedAt()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(result.releasedMemberId()).isEqualTo(10L);
        assertThat(result.missions()).hasSize(1);
        assertThat(result.missions().get(0).originalWorkbookMissionId()).isEqualTo(300L);
        assertThat(result.missions().get(0).missionType()).isEqualTo(MissionType.LINK);
        assertThat(result.missions().get(0).isNecessary()).isTrue();
    }

    private OriginalWorkbook originalWorkbook() {
        Curriculum curriculum = Curriculum.create(9L, ChallengerPart.SPRINGBOOT, "9기 스프링부트");
        WeeklyCurriculum weeklyCurriculum = WeeklyCurriculum.create(
            curriculum,
            1L,
            false,
            "1주차",
            Instant.parse("2026-06-01T00:00:00Z"),
            Instant.parse("2026-06-07T00:00:00Z")
        );
        OriginalWorkbook workbook = OriginalWorkbook.createAsReady(
            weeklyCurriculum,
            "1주차 워크북",
            "상세 설명",
            "https://workbook.example.com",
            "워크북 본문",
            OriginalWorkbookType.MAIN
        );
        workbook.changeStatus(OriginalWorkbookStatus.RELEASED, 10L);
        ReflectionTestUtils.setField(workbook, "id", 200L);
        ReflectionTestUtils.setField(workbook, "releasedAt", Instant.parse("2026-06-01T00:00:00Z"));
        return workbook;
    }
}
