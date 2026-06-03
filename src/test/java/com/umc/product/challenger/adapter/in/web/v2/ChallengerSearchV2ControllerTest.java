package com.umc.product.challenger.adapter.in.web.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.ChallengerSearchItemV2Info;
import com.umc.product.member.application.port.in.query.dto.ChallengerSearchV2Result;
import java.util.List;
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

@WebMvcTest(controllers = ChallengerSearchV2Controller.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerSearchV2Controller")
class ChallengerSearchV2ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    SearchMemberUseCase searchMemberUseCase;

    @Test
    @DisplayName("챌린저 검색 v2 응답은 이메일을 마스킹한다")
    void 챌린저_검색_v2_응답은_이메일을_마스킹한다() throws Exception {
        given(searchMemberUseCase.searchChallengersByV2(any(), any())).willReturn(new ChallengerSearchV2Result(
            new PageImpl<>(
                List.of(new ChallengerSearchItemV2Info(
                    1L, "홍길동", "길동", "gildong@example.com",
                    10L, "테스트대학교", null, 100L, 9L, 10L,
                    ChallengerPart.SPRINGBOOT, ChallengerStatus.ACTIVE, List.of(), false
                )),
                PageRequest.of(0, 10),
                1
            )
        ));

        mockMvc.perform(get("/api/v2/challenger/search")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.page.content[0].email").value("gil****@example.com"));
    }
}
