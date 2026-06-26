package com.umc.product.curriculum.adapter.in.web.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.GetOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.GetWeeklyBestWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.ChallengerWorkbookInfo;
import com.umc.product.curriculum.application.port.in.query.dto.GetBestWorkbooksQuery;
import com.umc.product.curriculum.application.port.in.query.dto.OriginalWorkbookInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WeeklyBestWorkbookInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WeeklyBestWorkbookPageInfo;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;

@WebMvcTest(controllers = WorkbookQueryV2Controller.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkbookQueryV2ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    GetOriginalWorkbookUseCase getOriginalWorkbookUseCase;

    @MockitoBean
    GetChallengerWorkbookUseCase getChallengerWorkbookUseCase;

    @MockitoBean
    GetWeeklyBestWorkbookUseCase getWeeklyBestWorkbookUseCase;

    @Test
    @DisplayName("원본 워크북 상세 조회 API는 UseCase 결과를 응답으로 변환한다")
    void getOriginalWorkbookSuccess() throws Exception {
        // given
        OriginalWorkbookInfo info = OriginalWorkbookInfo.builder()
            .originalWorkbookId(200L)
            .title("1주차 워크북")
            .description("설명")
            .url("https://workbook.example.com")
            .content("본문")
            .type(OriginalWorkbookType.MAIN)
            .status(OriginalWorkbookStatus.RELEASED)
            .missions(List.of(OriginalWorkbookInfo.OriginalWorkbookMissionInfo.builder()
                .originalWorkbookMissionId(300L)
                .title("미션 1")
                .description("미션 설명")
                .missionType(MissionType.LINK)
                .isNecessary(true)
                .build()))
            .build();
        given(getOriginalWorkbookUseCase.getById(200L)).willReturn(info);

        // when & then
        mockMvc.perform(get("/api/v2/curriculums/original-workbooks/{originalWorkbookId}", 200L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.originalWorkbookId").value(200L))
            .andExpect(jsonPath("$.result.missions[0].originalWorkbookMissionId").value(300L));
    }

    @Test
    @DisplayName("챌린저 워크북 상세 조회 API는 UseCase 결과를 응답으로 변환한다")
    void getChallengerWorkbookSuccess() throws Exception {
        // given
        ChallengerWorkbookInfo info = ChallengerWorkbookInfo.builder()
            .challengerWorkbookId(10L)
            .originalWorkbookId(200L)
            .receivedStudyGroupId(30L)
            .challengerId(40L)
            .isExcused(false)
            .content("본문")
            .isBestWorkbook(false)
            .submissions(List.of())
            .build();
        given(getChallengerWorkbookUseCase.getById(10L)).willReturn(info);

        // when & then
        mockMvc.perform(get("/api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}", 10L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.challengerWorkbookId").value(10L))
            .andExpect(jsonPath("$.result.hasSubmission").value(false));
    }

    @Test
    @DisplayName("베스트 워크북 조회 API는 필터를 Query로 변환하고 커서 응답을 반환한다")
    void getBestWorkbooksSuccess() throws Exception {
        // given
        WeeklyBestWorkbookInfo info = WeeklyBestWorkbookInfo.builder()
            .weeklyBestWorkbookEntityId(100L)
            .challengerId(40L)
            .gisuId(9L)
            .part(ChallengerPart.SPRINGBOOT)
            .studyGroupId(30L)
            .decidedMemberId(99L)
            .reason("잘 작성했습니다")
            .challengerWorkbooks(List.of())
            .build();
        given(getWeeklyBestWorkbookUseCase.searchBestWorkbooks(any(GetBestWorkbooksQuery.class)))
            .willReturn(WeeklyBestWorkbookPageInfo.of(List.of(info), 1));

        // when & then
        mockMvc.perform(get("/api/v2/curriculums/weekly-best-workbooks")
                .param("gisuId", "9")
                .param("schoolIds", "1", "2")
                .param("parts", "SPRINGBOOT")
                .param("weekNos", "1")
                .param("studyGroupIds", "30")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].weeklyBestWorkbookEntityId").value(100L))
            .andExpect(jsonPath("$.result.content[0].memberId").value(40L))
            .andExpect(jsonPath("$.result.hasNext").value(false));

        ArgumentCaptor<GetBestWorkbooksQuery> captor = ArgumentCaptor.forClass(GetBestWorkbooksQuery.class);
        then(getWeeklyBestWorkbookUseCase).should().searchBestWorkbooks(captor.capture());
        GetBestWorkbooksQuery query = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(query.gisuId()).isEqualTo(9L);
        org.assertj.core.api.Assertions.assertThat(query.schoolIds()).containsExactlyInAnyOrder(1L, 2L);
        org.assertj.core.api.Assertions.assertThat(query.parts()).containsExactly(ChallengerPart.SPRINGBOOT);
        org.assertj.core.api.Assertions.assertThat(query.weekNos()).containsExactly(1L);
        org.assertj.core.api.Assertions.assertThat(query.studyGroupIds()).containsExactly(30L);
        org.assertj.core.api.Assertions.assertThat(query.size()).isEqualTo(1);
    }
}
