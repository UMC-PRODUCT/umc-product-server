package com.umc.product.project.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.adapter.in.web.dto.request.AbortProjectRequest;
import com.umc.product.project.application.port.in.command.AbortProjectUseCase;
import com.umc.product.project.application.port.in.command.AddProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.CreateDraftProjectUseCase;
import com.umc.product.project.application.port.in.command.DeleteProjectUseCase;
import com.umc.product.project.application.port.in.command.PublishProjectUseCase;
import com.umc.product.project.application.port.in.command.RemoveProjectMemberUseCase;
import com.umc.product.project.application.port.in.command.SubmitProjectUseCase;
import com.umc.product.project.application.port.in.command.TransferProjectOwnershipUseCase;
import com.umc.product.project.application.port.in.command.UpdatePartQuotasUseCase;
import com.umc.product.project.application.port.in.command.UpdateProjectUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProjectCommandController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectCommandControllerTest {

    private static final Long TEST_MEMBER_ID = 99L;
    private static final Long PROJECT_ID = 42L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    CreateDraftProjectUseCase createDraftProjectUseCase;
    @MockitoBean
    UpdateProjectUseCase updateProjectUseCase;
    @MockitoBean
    SubmitProjectUseCase submitProjectUseCase;
    @MockitoBean
    TransferProjectOwnershipUseCase transferProjectOwnershipUseCase;
    @MockitoBean
    AddProjectMemberUseCase addProjectMemberUseCase;
    @MockitoBean
    RemoveProjectMemberUseCase removeProjectMemberUseCase;
    @MockitoBean
    UpdatePartQuotasUseCase updatePartQuotasUseCase;
    @MockitoBean
    PublishProjectUseCase publishProjectUseCase;
    @MockitoBean
    DeleteProjectUseCase deleteProjectUseCase;
    @MockitoBean
    AbortProjectUseCase abortProjectUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        MemberPrincipal principal = MemberPrincipal.builder()
            .memberId(TEST_MEMBER_ID)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    void DELETE_프로젝트_삭제_204() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/" + PROJECT_ID))
            .andExpect(status().isOk());

        then(deleteProjectUseCase).should().delete(any());
    }

    @Test
    void POST_프로젝트_중단_204() throws Exception {
        AbortProjectRequest request = new AbortProjectRequest("팀이 데모데이 불참으로 와해됨");

        mockMvc.perform(post("/api/v1/projects/" + PROJECT_ID + "/abort")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        then(abortProjectUseCase).should().abort(any());
    }

    @Test
    void POST_프로젝트_중단_reason_누락이면_400() throws Exception {
        AbortProjectRequest request = new AbortProjectRequest("  ");

        mockMvc.perform(post("/api/v1/projects/" + PROJECT_ID + "/abort")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        then(abortProjectUseCase).should(never()).abort(any());
    }

    @Test
    void POST_프로젝트_중단_body_누락이면_400() throws Exception {
        mockMvc.perform(post("/api/v1/projects/" + PROJECT_ID + "/abort")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        then(abortProjectUseCase).should(never()).abort(any());
    }
}
