package com.umc.product.challenger.adapter.in.web;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;

@WebMvcTest(controllers = ChallengerSearchController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerSearchController")
class ChallengerSearchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    SearchChallengerUseCase searchChallengerUseCase;

    @Test
    @DisplayName("커서 검색 size는 최대 50으로 제한되고 집계가 없으면 신규 파트도 0명으로 반환한다")
    void 커서_검색_size를_제한하고_집계가_없는_신규_파트도_반환한다() throws Exception {
        given(searchChallengerUseCase.cursorSearch(any(), eq(null), eq(50)))
            .willReturn(new SearchChallengerCursorResult(List.of(), null, false, Map.of()));

        mockMvc.perform(get("/api/v1/challenger/search/cursor")
                .param("size", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.cursor.hasNext").value(false))
            .andExpect(jsonPath("$.result.partCounts.length()").value(9))
            .andExpect(jsonPath("$.result.partCounts[7].part").value("WEB_PRODUCT_ENGINEER"))
            .andExpect(jsonPath("$.result.partCounts[7].count").value("0"))
            .andExpect(jsonPath("$.result.partCounts[8].part").value("MOBILE_PRODUCT_ENGINEER"))
            .andExpect(jsonPath("$.result.partCounts[8].count").value("0"));

        then(searchChallengerUseCase).should().cursorSearch(any(), eq(null), eq(50));
    }

    @Test
    @DisplayName("offset 검색 결과와 신규 파트를 포함한 파트별 집계를 기존 순서 뒤에 반환한다")
    void offset_검색_결과와_신규_파트를_포함한_파트별_집계를_반환한다() throws Exception {
        given(searchChallengerUseCase.offsetSearch(any(), any())).willReturn(new SearchChallengerResult(
            new PageImpl<>(
                List.of(new SearchChallengerItemInfo(
                    100L, 1L, 9L, 10L, ChallengerPart.SPRINGBOOT,
                    "홍길동", "길동", "테스트대학교", 1.0, null, List.of()
                )),
                PageRequest.of(0, 10),
                1
            ),
            Map.of(
                ChallengerPart.SPRINGBOOT, 1L,
                ChallengerPart.WEB_PRODUCT_ENGINEER, 2L,
                ChallengerPart.MOBILE_PRODUCT_ENGINEER, 3L,
                ChallengerPart.ADMIN, 4L
            )
        ));

        mockMvc.perform(get("/api/v1/challenger/search/offset")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.page.content[0].challengerId").value(100L))
            .andExpect(jsonPath("$.result.partCounts[*].part").value(contains(
                "PLAN", "DESIGN", "WEB", "ANDROID", "IOS", "NODEJS", "SPRINGBOOT",
                "WEB_PRODUCT_ENGINEER", "MOBILE_PRODUCT_ENGINEER"
            )))
            .andExpect(jsonPath("$.result.partCounts[*].count").value(contains(
                "0", "0", "0", "0", "0", "0", "1", "2", "3"
            )));
    }
}
