package com.umc.product.challenger.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
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
    @DisplayName("커서 검색 size는 최대 50으로 제한된다")
    void 커서_검색_size는_최대_50으로_제한된다() throws Exception {
        given(searchChallengerUseCase.cursorSearch(any(), eq(null), eq(50)))
            .willReturn(new SearchChallengerCursorResult(List.of(), null, false, Map.of()));

        mockMvc.perform(get("/api/v1/challenger/search/cursor")
                .param("size", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.cursor.hasNext").value(false));

        then(searchChallengerUseCase).should().cursorSearch(any(), eq(null), eq(50));
    }

    @Test
    @DisplayName("offset 검색 결과를 반환한다")
    void offset_검색_결과를_반환한다() throws Exception {
        given(searchChallengerUseCase.offsetSearch(any(), any())).willReturn(new SearchChallengerResult(
            new PageImpl<>(
                List.of(new SearchChallengerItemInfo(
                    100L, 1L, 9L, 10L, ChallengerPart.SPRINGBOOT,
                    "홍길동", "길동", "테스트대학교", 1.0, null, List.of()
                )),
                PageRequest.of(0, 10),
                1
            ),
            Map.of(ChallengerPart.SPRINGBOOT, 1L)
        ));

        mockMvc.perform(get("/api/v1/challenger/search/offset")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.page.content[0].challengerId").value(100L));
    }
}
