package com.umc.product.feedback.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.feedback.application.port.in.command.SubmitUserFeedbackResponseUseCase;
import com.umc.product.feedback.application.port.in.query.GetUserFeedbackTemplateUseCase;
import com.umc.product.feedback.application.port.in.query.dto.UserFeedbackTemplateInfo;
import com.umc.product.feedback.domain.enums.UserFeedbackContext;
import com.umc.product.feedback.domain.enums.UserFeedbackTargetType;
import com.umc.product.form.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.form.domain.enums.QuestionType;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;

@WebMvcTest(controllers = UserFeedbackController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserFeedbackControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    GetUserFeedbackTemplateUseCase getUserFeedbackTemplateUseCase;
    @MockitoBean
    SubmitUserFeedbackResponseUseCase submitUserFeedbackResponseUseCase;

    @BeforeEach
    void setUp() {
        MemberPrincipal principal = MemberPrincipal.builder().memberId(1L).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("미제출이면 기존 템플릿과 문항 구조를 반환한다")
    void 미제출_폼_응답() throws Exception {
        // Given
        FormWithStructureInfo form = FormWithStructureInfo.builder()
            .formId(201L).title("프로젝트 지원 경험이 어떠셨나요?")
            .sections(List.of(FormWithStructureInfo.SectionWithQuestions.builder()
                .sectionId(301L).title("사용자 조사").orderNo(1L)
                .questions(List.of(FormWithStructureInfo.QuestionWithOptions.builder()
                    .questionId(401L).title("지원 과정은 어떠셨나요?").type(QuestionType.RADIO)
                    .isRequired(true).orderNo(1L)
                    .options(List.of(FormWithStructureInfo.Option.builder()
                        .optionId(501L).content("쉬워요").orderNo(1L).isOther(false).build()))
                    .build()))
                .build()))
            .build();
        given(getUserFeedbackTemplateUseCase.findTemplate(1L, UserFeedbackContext.APPLICATION_SUBMITTED))
            .willReturn(Optional.of(UserFeedbackTemplateInfo.builder()
                .templateId(101L).context(UserFeedbackContext.APPLICATION_SUBMITTED)
                .targetType(UserFeedbackTargetType.NEW_CHALLENGER).form(form).build()));

        // When / Then
        mockMvc.perform(get("/api/v1/user-feedbacks/templates").param("context", "APPLICATION_SUBMITTED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result.submitted").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.result.templateId").value("101"))
            .andExpect(jsonPath("$.result.context").value("APPLICATION_SUBMITTED"))
            .andExpect(jsonPath("$.result.targetType").value("NEW_CHALLENGER"))
            .andExpect(jsonPath("$.result.form.formId").value("201"))
            .andExpect(jsonPath("$.result.form.sections[0].questions[0].questionId").value("401"))
            .andExpect(jsonPath("$.result.form.sections[0].questions[0].isRequired").value(true))
            .andExpect(jsonPath("$.result.form.sections[0].questions[0].options[0].optionId").value("501"));
        then(getUserFeedbackTemplateUseCase).should().findTemplate(1L, UserFeedbackContext.APPLICATION_SUBMITTED);
    }

    @Test
    @DisplayName("제출 완료로 조회 결과가 없으면 result를 생략한 성공 응답을 반환한다")
    void 제출_완료_응답() throws Exception {
        // Given
        given(getUserFeedbackTemplateUseCase.findTemplate(1L, UserFeedbackContext.MATCHING_COMPLETED))
            .willReturn(Optional.empty());

        // When / Then
        mockMvc.perform(get("/api/v1/user-feedbacks/templates").param("context", "MATCHING_COMPLETED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result").doesNotHaveJsonPath());
    }

    @Test
    @DisplayName("템플릿이 없으면 result 필드를 생략한다")
    void 템플릿_없음_응답() throws Exception {
        // Given
        given(getUserFeedbackTemplateUseCase.findTemplate(1L, UserFeedbackContext.APPLICATION_SUBMITTED))
            .willReturn(Optional.empty());

        // When / Then
        mockMvc.perform(get("/api/v1/user-feedbacks/templates").param("context", "APPLICATION_SUBMITTED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.message").value("성공입니다."))
            .andExpect(jsonPath("$.result").doesNotHaveJsonPath());
    }
}
