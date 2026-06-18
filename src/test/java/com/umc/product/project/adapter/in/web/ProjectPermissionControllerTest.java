package com.umc.product.project.adapter.in.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.project.application.port.in.query.GetProjectPermissionsUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionCapabilityInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.ApplicationFormPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.ApplicationPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.MemberPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.PartQuotaPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.StatisticsPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo.StatusPermissions;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionReason;

@WebMvcTest(controllers = ProjectPermissionController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectPermissionControllerTest {

    private static final Long TEST_MEMBER_ID = 99L;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    GetProjectPermissionsUseCase getProjectPermissionsUseCase;

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
    void GET_permissions_ids가_없으면_400() throws Exception {
        mockMvc.perform(get("/api/v1/projects/permissions"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void GET_permissions_ids가_100개를_넘으면_400() throws Exception {
        var request = get("/api/v1/projects/permissions");
        for (int i = 1; i <= 101; i++) {
            request.param("ids", String.valueOf(i));
        }

        mockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
    void GET_permissions_중복_id를_순서_보존하며_제거해서_조회한다() throws Exception {
        given(getProjectPermissionsUseCase.listByProjectIds(eq(TEST_MEMBER_ID), eq(List.of(1L, 2L))))
            .willReturn(List.of(permissionInfo(1L)));

        mockMvc.perform(get("/api/v1/projects/permissions")
                .param("ids", "1", "2", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.projects[0].projectId").value(1))
            .andExpect(jsonPath("$.result.projects[0].exists").value(true))
            .andExpect(jsonPath("$.result.projects[0].canEditInfo.allowed").value(true))
            .andExpect(jsonPath("$.result.projects[0].applicationForm.canPublish.reasonCode")
                .value("NOT_IMPLEMENTED"));

        then(getProjectPermissionsUseCase).should()
            .listByProjectIds(TEST_MEMBER_ID, List.of(1L, 2L));
    }

    private ProjectPermissionInfo permissionInfo(Long projectId) {
        ProjectPermissionCapabilityInfo allowed = ProjectPermissionCapabilityInfo.allow();
        ProjectPermissionCapabilityInfo notImplemented =
            ProjectPermissionCapabilityInfo.denied(ProjectPermissionReason.NOT_IMPLEMENTED);

        return new ProjectPermissionInfo(
            projectId,
            true,
            allowed,
            allowed,
            allowed,
            new ApplicationFormPermissions(allowed, allowed, allowed, notImplemented, notImplemented),
            new PartQuotaPermissions(allowed),
            new StatusPermissions(allowed, allowed, notImplemented, allowed),
            new ApplicationPermissions(allowed, allowed, allowed),
            new MemberPermissions(allowed, allowed, allowed),
            new StatisticsPermissions(allowed)
        );
    }
}
