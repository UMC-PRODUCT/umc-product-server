package com.umc.product.authorization.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.authorization.application.port.in.query.ResourcePermissionUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@WebMvcTest(controllers = ResourcePermissionController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ResourcePermissionController")
class ResourcePermissionControllerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long RESOURCE_ID = 100L;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    ResourcePermissionUseCase resourcePermissionUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        MemberPrincipal principal = MemberPrincipal.builder().memberId(MEMBER_ID).build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("특정 리소스와 특정 권한을 지정해 권한을 조회한다")
    void 특정_리소스와_특정_권한을_지정해_권한을_조회한다() throws Exception {
        // given
        given(resourcePermissionUseCase.hasPermission(
            MEMBER_ID,
            ResourceType.NOTICE,
            RESOURCE_ID,
            PermissionType.READ
        )).willReturn(new ResourcePermissionInfo(
            ResourceType.NOTICE,
            RESOURCE_ID,
            Map.of(PermissionType.READ, true)
        ));

        // when & then
        mockMvc.perform(get("/api/v1/authorization/resource-permission")
                .param("resourceType", "NOTICE")
                .param("resourceId", String.valueOf(RESOURCE_ID))
                .param("permissionType", "READ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.resourceType").value("NOTICE"))
            .andExpect(jsonPath("$.result.resourceId").value(RESOURCE_ID))
            .andExpect(jsonPath("$.result.permissions[0].permissionType").value("READ"))
            .andExpect(jsonPath("$.result.permissions[0].hasPermission").value(true));

        verify(resourcePermissionUseCase).hasPermission(
            eq(MEMBER_ID),
            eq(ResourceType.NOTICE),
            eq(RESOURCE_ID),
            eq(PermissionType.READ)
        );
    }

    @Test
    @DisplayName("resourceId 없이 특정 권한을 지정해 타입 단위 권한을 조회한다")
    void resourceId_없이_특정_권한을_지정해_타입_단위_권한을_조회한다() throws Exception {
        // given
        given(resourcePermissionUseCase.hasPermission(
            MEMBER_ID,
            ResourceType.NOTICE,
            null,
            PermissionType.READ
        )).willReturn(new ResourcePermissionInfo(
            ResourceType.NOTICE,
            null,
            Map.of(PermissionType.READ, true)
        ));

        // when & then
        mockMvc.perform(get("/api/v1/authorization/resource-permission")
                .param("resourceType", "NOTICE")
                .param("permissionType", "READ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.resourceType").value("NOTICE"))
            .andExpect(jsonPath("$.result.resourceId").doesNotExist())
            .andExpect(jsonPath("$.result.permissions[0].permissionType").value("READ"))
            .andExpect(jsonPath("$.result.permissions[0].hasPermission").value(true));

        verify(resourcePermissionUseCase).hasPermission(
            eq(MEMBER_ID),
            eq(ResourceType.NOTICE),
            eq(null),
            eq(PermissionType.READ)
        );
    }

    @Test
    @DisplayName("GET 요청에 resourceType이 없으면 실패한다")
    void GET_요청에_resourceType이_없으면_실패한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/authorization/resource-permission")
                .param("resourceId", String.valueOf(RESOURCE_ID))
                .param("permissionType", "READ"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("COMMON-400"));

        verifyNoInteractions(resourcePermissionUseCase);
    }

    @Test
    @DisplayName("GET 요청에 유효하지 않은 enum 값이 있으면 실패한다")
    void GET_요청에_유효하지_않은_enum_값이_있으면_실패한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/authorization/resource-permission")
                .param("resourceType", "NOTICE")
                .param("permissionType", "INVALID"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("COMMON-400"));

        verifyNoInteractions(resourcePermissionUseCase);
    }

    @Test
    @DisplayName("여러 리소스에 대한 권한을 배치로 조회한다")
    void 여러_리소스에_대한_권한을_배치로_조회한다() throws Exception {
        // given
        given(resourcePermissionUseCase.batchHasPermission(eq(MEMBER_ID), any()))
            .willReturn(java.util.List.of(
                new ResourcePermissionInfo(
                    ResourceType.NOTICE,
                    100L,
                    Map.of(PermissionType.READ, true)
                ),
                new ResourcePermissionInfo(
                    ResourceType.NOTICE,
                    101L,
                    Map.of(PermissionType.READ, false)
                )
            ));

        String requestBody = """
            {
              "queries": [
                {
                  "resourceType": "NOTICE",
                  "resourceIds": [100, 101],
                  "permissionTypes": ["READ"]
                }
              ]
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/authorization/resource-permissions/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.results[0].resourceType").value("NOTICE"))
            .andExpect(jsonPath("$.result.results[0].resourceId").value(100L))
            .andExpect(jsonPath("$.result.results[0].permissions[0].permissionType").value("READ"))
            .andExpect(jsonPath("$.result.results[0].permissions[0].hasPermission").value(true))
            .andExpect(jsonPath("$.result.results[1].resourceType").value("NOTICE"))
            .andExpect(jsonPath("$.result.results[1].resourceId").value(101L))
            .andExpect(jsonPath("$.result.results[1].permissions[0].hasPermission").value(false));
    }

    @Test
    @DisplayName("배치 요청에 resourceType 없이 resourceIds만 있으면 실패한다")
    void 배치_요청에_resourceType_없이_resourceIds만_있으면_실패한다() throws Exception {
        // given
        String requestBody = """
            {
              "queries": [
                {
                  "resourceIds": [100],
                  "permissionTypes": ["READ"]
                }
              ]
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/authorization/resource-permissions/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("COMMON-400"));

        verifyNoInteractions(resourcePermissionUseCase);
    }

    @Test
    @DisplayName("배치 요청에 resourceIds가 빈 배열이면 실패한다")
    void 배치_요청에_resourceIds가_빈_배열이면_실패한다() throws Exception {
        // given
        String requestBody = """
            {
              "queries": [
                {
                  "resourceType": "NOTICE",
                  "resourceIds": [],
                  "permissionTypes": ["READ"]
                }
              ]
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/authorization/resource-permissions/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("COMMON-400"));

        verifyNoInteractions(resourcePermissionUseCase);
    }

    @Test
    @DisplayName("배치 요청에 permissionTypes가 빈 배열이면 실패한다")
    void 배치_요청에_permissionTypes가_빈_배열이면_실패한다() throws Exception {
        // given
        String requestBody = """
            {
              "queries": [
                {
                  "resourceType": "NOTICE",
                  "resourceIds": [100],
                  "permissionTypes": []
                }
              ]
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/authorization/resource-permissions/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("COMMON-400"));

        verifyNoInteractions(resourcePermissionUseCase);
    }
}
