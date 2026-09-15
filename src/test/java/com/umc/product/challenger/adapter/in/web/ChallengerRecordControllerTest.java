package com.umc.product.challenger.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.challenger.adapter.in.web.assembler.ChallengerRecordResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerRecordResponse;
import com.umc.product.challenger.application.port.in.command.ManageChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;

@WebMvcTest(controllers = ChallengerRecordController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerRecordController")
class ChallengerRecordControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    ChallengerRecordResponseAssembler assembler;

    @MockitoBean
    ManageChallengerRecordUseCase manageChallengerRecordUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        MemberPrincipal principal = MemberPrincipal.builder().memberId(99L).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("코드로 챌린저 기록을 조회한다")
    void 코드로_챌린저_기록을_조회한다() throws Exception {
        given(assembler.from("ABC123")).willReturn(ChallengerRecordResponse.builder()
            .code("ABC123").part(ChallengerPart.SPRINGBOOT).build());

        mockMvc.perform(get("/api/v1/challenger-record/code/{code}", "ABC123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.code").value("ABC123"));
    }

    @Test
    @DisplayName("단일 파트와 인프라 여부로 코드를 발급한다")
    void 단일_파트와_인프라_여부로_코드를_발급한다() throws Exception {
        given(manageChallengerRecordUseCase.create(any())).willReturn(10L);
        given(assembler.from(10L)).willReturn(ChallengerRecordResponse.builder()
            .code("ABC123").part(ChallengerPart.WEB_PRODUCT_ENGINEER).infra(true).build());

        mockMvc.perform(post("/api/v1/challenger-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"gisuId":1,"chapterId":2,"schoolId":3,
                     "part":"WEB_PRODUCT_ENGINEER","infra":true,"memberName":"홍길동"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.part").value("WEB_PRODUCT_ENGINEER"))
            .andExpect(jsonPath("$.result.infra").value(true));

        ArgumentCaptor<CreateChallengerRecordCommand> captor =
            ArgumentCaptor.forClass(CreateChallengerRecordCommand.class);
        then(manageChallengerRecordUseCase).should().create(captor.capture());
        assertThat(captor.getValue().part()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(captor.getValue().infra()).isTrue();
        assertThat(captor.getValue().creatorMemberId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("일괄 발급에서 수강자와 비수강 중앙 운영진을 전달한다")
    void 일괄_발급에서_수강자와_비수강_중앙_운영진을_전달한다() throws Exception {
        given(manageChallengerRecordUseCase.createBulk(any())).willReturn(List.of(10L, 11L));

        mockMvc.perform(post("/api/v1/challenger-record/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    [{"gisuId":1,"chapterId":2,"schoolId":3,"part":"PLAN","memberName":"홍길동"},
                     {"gisuId":1,"schoolId":3,"memberName":"김철수",
                      "challengerRoleType":"CENTRAL_OPERATING_TEAM_MEMBER"}]
                    """))
            .andExpect(status().isOk());

        ArgumentCaptor<List<CreateChallengerRecordCommand>> captor = ArgumentCaptor.captor();
        then(manageChallengerRecordUseCase).should().createBulk(captor.capture());
        assertThat(captor.getValue().getFirst().part()).isEqualTo(ChallengerPart.PLAN);
        assertThat(captor.getValue().getLast().part()).isNull();
        assertThat(captor.getValue().getLast().chapterId()).isNull();
        assertThat(captor.getValue().getLast().toEntity().canOmitChapter()).isTrue();
    }

    @Test
    @DisplayName("개발 파트가 아니면 인프라 선택을 거부한다")
    void 개발_파트가_아니면_인프라_선택을_거부한다() throws Exception {
        mockMvc.perform(post("/api/v1/challenger-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"gisuId":1,"chapterId":2,"schoolId":3,
                     "part":"DESIGN","infra":true,"memberName":"홍길동"}
                    """))
            .andExpect(status().isBadRequest());

        then(manageChallengerRecordUseCase).should(never()).create(any());
    }

    @Test
    @DisplayName("일반 수강 코드에 파트가 없으면 거부한다")
    void 일반_수강_코드에_파트가_없으면_거부한다() throws Exception {
        mockMvc.perform(post("/api/v1/challenger-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"gisuId":1,"chapterId":2,"schoolId":3,"memberName":"홍길동"}
                    """))
            .andExpect(status().isBadRequest());

        then(manageChallengerRecordUseCase).should(never()).create(any());
    }

    @Test
    @DisplayName("기록 생성 요청의 gisuId가 없으면 거부한다")
    void 기록_생성_요청의_gisuId가_없으면_거부한다() throws Exception {
        mockMvc.perform(post("/api/v1/challenger-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"chapterId":2,"schoolId":3,"part":"SPRINGBOOT","memberName":"홍길동"}
                    """))
            .andExpect(status().isBadRequest());

        then(manageChallengerRecordUseCase).should(never()).create(any());
    }

    @Test
    @DisplayName("챌린저 기록 생성 요청에 SUPER_ADMIN을 입력하면 거부한다")
    void 챌린저_기록_생성_요청에_super_admin을_입력하면_거부한다() throws Exception {
        mockMvc.perform(post("/api/v1/challenger-record")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"gisuId":1,"chapterId":2,"schoolId":3,"part":"SPRINGBOOT",
                     "memberName":"홍길동","challengerRoleType":"SUPER_ADMIN"}
                    """))
            .andExpect(status().isBadRequest());

        then(manageChallengerRecordUseCase).should(never()).create(any());
    }
}
