package com.umc.product.inquiry.adapter.in.web;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.InquiryFixture;
import com.umc.product.support.fixture.MemberFixture;

@DisplayName("문의 운영 관리 API")
class InquiryStaffApiIntegrationTest extends IntegrationTestSupport {

    private static final String AUTHOR = "author-token";
    private static final String STAFF = "staff-token";
    private static final String OTHER_STAFF = "other-staff-token";
    private static final String SUPER_ADMIN = "super-admin-token";

    @Autowired
    MemberFixture memberFixture;
    @Autowired
    InquiryFixture inquiryFixture;
    @Autowired
    RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;
    @Autowired
    JdbcTemplate jdbcTemplate;

    private Long authorId;
    private Long staffId;
    private Long otherStaffId;
    private Long superAdminId;
    private Long accountCategoryId;
    private Long attendanceCategoryId;

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        authorId = memberFixture.일반("author").getId();
        staffId = memberFixture.일반("staff").getId();
        otherStaffId = memberFixture.일반("manager").getId();
        superAdminId = memberFixture.일반("super-admin").getId();
        memberFixture.시스템_역할_부여(staffId, MemberSystemRoleType.UMC_PRODUCT);
        memberFixture.시스템_역할_부여(otherStaffId, MemberSystemRoleType.UMC_PRODUCT);
        memberFixture.시스템_역할_부여(superAdminId, MemberSystemRoleType.SUPER_ADMIN);
        accountCategoryId = inquiryFixture.카테고리("계정·로그인", 2).getId();
        attendanceCategoryId = inquiryFixture.카테고리("출석·과제", 1).getId();
        토큰(AUTHOR, authorId);
        토큰(STAFF, staffId);
        토큰(OTHER_STAFF, otherStaffId);
        토큰(SUPER_ADMIN, superAdminId);
    }

    @Test
    @DisplayName("받는 팀 7개와 담당 범위를 돌려주고 맨 뒤에 모르겠어요를 붙인다")
    void 받는_팀_목록() throws Exception {
        mockMvc.perform(get("/api/v1/inquiries/teams").header("Authorization", "Bearer " + AUTHOR))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", hasSize(8)))
            .andExpect(jsonPath("$.result[0].team").value("BRAND_GROWTH"))
            .andExpect(jsonPath("$.result[0].name").value("Brand & Growth"))
            .andExpect(jsonPath("$.result[0].description").value("모집·선발, 공식 홈페이지, 테크 블로그"))
            .andExpect(jsonPath("$.result[7].team").value(nullValue()))
            .andExpect(jsonPath("$.result[7].name").value("어느 팀인지 모르겠어요"));
    }

    @Test
    @DisplayName("담당자 후보는 UMC_PRODUCT 회원만 이름순이고, 운영 API는 운영진이 아니면 403이다")
    void 담당자_후보와_권한() throws Exception {
        운영(get("/api/v1/inquiries/admin/assignee-candidates"), STAFF)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[*].memberId", contains(otherStaffId.toString(), staffId.toString())));

        운영(get("/api/v1/inquiries/admin"), AUTHOR)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("INQUIRY-0003"));
        운영(get("/api/v1/inquiries/admin/labels"), AUTHOR)
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("담당자를 지정하면 이력에 남고, 본 담당자와 지금 담당자가 다르면 409, 운영진이 아닌 사람은 지정할 수 없다")
    void 담당자_지정과_충돌() throws Exception {
        // given
        long inquiryId = 등록(accountCategoryId, "로그인이 안 돼요");

        // when & then
        담당자_변경(STAFF, inquiryId, staffId, null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.assignee.memberId").value(staffId))
            .andExpect(jsonPath("$.result.assignee.name").value("staff"));
        담당자_변경(OTHER_STAFF, inquiryId, otherStaffId, null)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INQUIRY-0015"));
        담당자_변경(OTHER_STAFF, inquiryId, otherStaffId, staffId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.assignee.memberId").value(otherStaffId));
        담당자_변경(STAFF, inquiryId, superAdminId, otherStaffId)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0016"));
        담당자_변경(STAFF, inquiryId, null, otherStaffId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.assignee").value(nullValue()))
            .andExpect(jsonPath("$.result.histories[*].type",
                contains("RECEIVED", "ASSIGNED", "REASSIGNED", "UNASSIGNED")))
            .andExpect(jsonPath("$.result.histories[0].actor.memberId").value(authorId))
            .andExpect(jsonPath("$.result.histories[1].actor.memberId").value(staffId))
            .andExpect(jsonPath("$.result.histories[1].previousAssignee").value(nullValue()))
            .andExpect(jsonPath("$.result.histories[1].newAssignee.memberId").value(staffId))
            .andExpect(jsonPath("$.result.histories[2].previousAssignee.memberId").value(staffId))
            .andExpect(jsonPath("$.result.histories[2].newAssignee.memberId").value(otherStaffId))
            .andExpect(jsonPath("$.result.histories[3].actor.memberId").value(staffId))
            .andExpect(jsonPath("$.result.inquiry.inquiryId").value(inquiryId));
    }

    @Test
    @DisplayName("담당자가 UMC_PRODUCT 역할을 잃으면 미배정으로 보이고, 본 담당자를 비운 채로 다시 지정할 수 있다")
    void 권한_잃은_담당자() throws Exception {
        // given
        long inquiryId = 등록(accountCategoryId, "로그인이 안 돼요");
        담당자_변경(STAFF, inquiryId, otherStaffId, null).andExpect(status().isOk());
        jdbcTemplate.update(
            "delete from member_system_role where member_id = ? and role_type = 'UMC_PRODUCT'", otherStaffId
        );

        // when & then
        운영(get("/api/v1/inquiries/admin/{inquiryId}", inquiryId), STAFF)
            .andExpect(jsonPath("$.result.assignee").value(nullValue()));
        운영(get("/api/v1/inquiries/admin").param("tab", "UNASSIGNED"), STAFF)
            .andExpect(jsonPath("$.result.counts.unassigned").value(1))
            .andExpect(jsonPath("$.result.inquiries[0].assignee").value(nullValue()));
        담당자_변경(STAFF, inquiryId, staffId, null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.assignee.memberId").value(staffId));
    }

    @Test
    @DisplayName("운영 목록은 탭별 개수를 같은 필터로 세고, 상태·카테고리·라벨로 거른다")
    void 운영_목록_탭과_필터() throws Exception {
        // given
        long mine = 등록(accountCategoryId, "내가 맡은 문의");
        long others = 등록(attendanceCategoryId, "다른 사람이 맡은 문의");
        long unassigned = 등록(accountCategoryId, "아직 아무도 안 맡은 문의");
        담당자_변경(STAFF, mine, staffId, null).andExpect(status().isOk());
        담당자_변경(OTHER_STAFF, others, otherStaffId, null).andExpect(status().isOk());
        long labelId = inquiryFixture.라벨("출석 오류").getId();
        라벨_적용(STAFF, others, List.of(labelId)).andExpect(status().isOk());
        운영(post("/api/v1/inquiries/admin/{inquiryId}/close", unassigned), STAFF).andExpect(status().isOk());

        // when & then
        운영(get("/api/v1/inquiries/admin").param("tab", "MINE"), STAFF)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.counts.all").value(3))
            .andExpect(jsonPath("$.result.counts.mine").value(1))
            .andExpect(jsonPath("$.result.counts.unassigned").value(1))
            .andExpect(jsonPath("$.result.inquiries[*].inquiryId", contains(String.valueOf(mine))));
        운영(get("/api/v1/inquiries/admin").param("categoryId", accountCategoryId.toString()), STAFF)
            .andExpect(jsonPath("$.result.counts.all").value(2))
            .andExpect(jsonPath("$.result.counts.mine").value(1))
            .andExpect(jsonPath("$.result.inquiries[*].inquiryId", contains(String.valueOf(unassigned), String.valueOf(mine))));
        운영(get("/api/v1/inquiries/admin").param("labelId", String.valueOf(labelId)), STAFF)
            .andExpect(jsonPath("$.result.inquiries[*].inquiryId", contains(String.valueOf(others))))
            .andExpect(jsonPath("$.result.inquiries[0].labels[0].name").value("출석 오류"))
            .andExpect(jsonPath("$.result.inquiries[0].assignee.name").value("manager"))
            .andExpect(jsonPath("$.result.inquiries[0].category.name").value("출석·과제"));
        운영(get("/api/v1/inquiries/admin").param("status", "CLOSED"), STAFF)
            .andExpect(jsonPath("$.result.inquiries[*].inquiryId", contains(String.valueOf(unassigned))));
        운영(get("/api/v1/inquiries/admin").param("size", "2"), STAFF)
            .andExpect(jsonPath("$.result.inquiries", hasSize(2)))
            .andExpect(jsonPath("$.result.hasNext").value(true));
    }

    @Test
    @DisplayName("라벨은 만들고 이름을 바꾸고 지울 수 있고, 지우면 문의에서도 빠진다. 이름이 겹치면 409다")
    void 라벨_관리() throws Exception {
        // given
        long inquiryId = 등록(accountCategoryId, "로그인이 안 돼요");

        // when
        long labelId = 라벨_만들기(" 확인 대기 ");
        라벨_만들기_요청("확인 대기")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INQUIRY-0018"));
        long otherLabelId = 라벨_만들기("중복 문의");
        운영(patch("/api/v1/inquiries/admin/labels/{labelId}", labelId), STAFF, Map.of("name", "중복 문의"))
            .andExpect(status().isConflict());
        운영(patch("/api/v1/inquiries/admin/labels/{labelId}", labelId), STAFF, Map.of("name", "가".repeat(31)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INQUIRY-0019"));
        운영(patch("/api/v1/inquiries/admin/labels/{labelId}", labelId), STAFF, Map.of("name", "확인 필요"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.name").value("확인 필요"));
        라벨_적용(STAFF, inquiryId, List.of(labelId, otherLabelId))
            .andExpect(jsonPath("$.result.labels[*].name", contains("중복 문의", "확인 필요")));
        라벨_적용(STAFF, inquiryId, List.of(labelId, 999_999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("INQUIRY-0017"));
        운영(delete("/api/v1/inquiries/admin/labels/{labelId}", otherLabelId), STAFF)
            .andExpect(status().isOk());

        // then
        운영(get("/api/v1/inquiries/admin/{inquiryId}", inquiryId), STAFF)
            .andExpect(jsonPath("$.result.labels[*].name", contains("확인 필요")));
        운영(get("/api/v1/inquiries/admin/labels"), STAFF)
            .andExpect(jsonPath("$.result[*].name", hasItem("확인 필요")))
            .andExpect(jsonPath("$.result[*].name", not(hasItem("중복 문의"))));
    }

    @Test
    @DisplayName("받는 팀을 바꾸면 이력에 남고 질문자 상세에도 보이며, 질문자 상세에는 라벨·담당자·이력이 없다")
    void 받는_팀_변경() throws Exception {
        // given
        long inquiryId = 등록(accountCategoryId, "로그인이 안 돼요");
        라벨_적용(STAFF, inquiryId, List.of(inquiryFixture.라벨("출석 오류").getId()));

        // when
        운영(put("/api/v1/inquiries/admin/{inquiryId}/team", inquiryId), STAFF, Map.of("targetTeam", "INFRASTRUCTURE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.histories[1].type").value("TEAM_CHANGED"))
            .andExpect(jsonPath("$.result.histories[1].previousTeam").value("PLATFORM"))
            .andExpect(jsonPath("$.result.histories[1].newTeam").value("INFRASTRUCTURE"));

        // then
        mockMvc.perform(get("/api/v1/inquiries/{inquiryId}", inquiryId).header("Authorization", "Bearer " + AUTHOR))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.targetTeam").value("INFRASTRUCTURE"))
            .andExpect(jsonPath("$.result.labels").doesNotExist())
            .andExpect(jsonPath("$.result.assignee").doesNotExist())
            .andExpect(jsonPath("$.result.histories").doesNotExist());
        운영(get("/api/v1/inquiries/admin/{inquiryId}", inquiryId), AUTHOR)
            .andExpect(status().isForbidden());
    }

    private ResultActions 담당자_변경(String token, long inquiryId, Long assigneeId, Long expectedId) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("assigneeMemberId", assigneeId);
        body.put("expectedAssigneeMemberId", expectedId);
        return 운영(put("/api/v1/inquiries/admin/{inquiryId}/assignee", inquiryId), token, body);
    }

    private ResultActions 라벨_적용(String token, long inquiryId, List<Long> labelIds) throws Exception {
        return 운영(put("/api/v1/inquiries/admin/{inquiryId}/labels", inquiryId), token, Map.of("labelIds", labelIds));
    }

    private long 라벨_만들기(String name) throws Exception {
        String response = 라벨_만들기_요청(name)
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).at("/result/labelId").asLong();
    }

    private ResultActions 라벨_만들기_요청(String name) throws Exception {
        return 운영(post("/api/v1/inquiries/admin/labels"), STAFF, Map.of("name", name));
    }

    private ResultActions 운영(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mockMvc.perform(request.header("Authorization", "Bearer " + token));
    }

    private ResultActions 운영(MockHttpServletRequestBuilder request, String token, Object body) throws Exception {
        return mockMvc.perform(request
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
    }

    private long 등록(Long categoryId, String content) throws Exception {
        String response = mockMvc.perform(post("/api/v1/inquiries")
                .header("Authorization", "Bearer " + AUTHOR)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "categoryId", categoryId,
                    "targetTeam", "PLATFORM",
                    "content", content,
                    "clientRequestId", UUID.randomUUID().toString()
                ))))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).at("/result/inquiryId").asLong();
    }

    private void 토큰(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }
}
