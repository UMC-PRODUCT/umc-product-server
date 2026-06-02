package com.umc.product.techblog.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChallengerRoleFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("TechBlogInteractionController 통합 테스트")
class TechBlogInteractionControllerIntegrationTest extends IntegrationTestSupport {

    private static final String CONTENT_URL = "/api/v1/tech-blog/contents/blog/spring-boot-tips";
    private static final String BASE_URL = CONTENT_URL + "/comments";

    @Autowired
    SaveMemberPort saveMemberPort;

    @Autowired
    ChallengerFixture challengerFixture;

    @Autowired
    GisuFixture gisuFixture;

    @Autowired
    ChapterFixture chapterFixture;

    @Autowired
    SchoolFixture schoolFixture;

    @Autowired
    ChallengerRoleFixture challengerRoleFixture;

    private String authorToken;
    private String otherToken;
    private String adminToken;

    @BeforeEach
    void setUpAuth() {
        Gisu gisu = gisuFixture.비활성_기수(99L);
        Chapter chapter = chapterFixture.지부(gisu);
        School adminSchool = schoolFixture.지부에_소속된_학교("tech-blog-admin-school", chapter);

        Long authorMemberId = createMember("author", adminSchool.getId());
        Long otherMemberId = createMember("other", adminSchool.getId());
        Long adminMemberId = createMember("admin", adminSchool.getId());

        challengerFixture.챌린저(authorMemberId, ChallengerPart.SPRINGBOOT, gisu.getId());
        challengerFixture.챌린저(otherMemberId, ChallengerPart.SPRINGBOOT, gisu.getId());
        Challenger adminChallenger = challengerFixture.챌린저(adminMemberId, ChallengerPart.SPRINGBOOT, gisu.getId());
        challengerRoleFixture.중앙운영사무국_총괄(adminChallenger.getId(), gisu.getId());

        authorToken = mockToken("author-token", authorMemberId);
        otherToken = mockToken("other-token", otherMemberId);
        adminToken = mockToken("admin-token", adminMemberId);
    }

    private Long createMember(String name, Long schoolId) {
        return saveMemberPort.save(Member.create(
            name,
            name,
            name + "-tech-blog@test.com",
            schoolId,
            null
        )).getId();
    }

    @Test
    @DisplayName("콘텐츠가 없어도 댓글 목록은 빈 커서 응답을 반환한다")
    void 콘텐츠가_없어도_댓글_목록은_빈_커서_응답을_반환한다() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content").isArray())
            .andExpect(jsonPath("$.result.content").isEmpty())
            .andExpect(jsonPath("$.result.nextCursor").doesNotExist())
            .andExpect(jsonPath("$.result.hasNext").value(false));
    }

    @Test
    @DisplayName("콘텐츠 좋아요는 없는 콘텐츠 조회 시 빈 상태를 반환하고 토글 시 콘텐츠를 생성한다")
    void 콘텐츠_좋아요는_없는_콘텐츠_조회_시_빈_상태를_반환하고_토글_시_콘텐츠를_생성한다() throws Exception {
        mockMvc.perform(get(CONTENT_URL + "/like"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.likedByMe").value(false))
            .andExpect(jsonPath("$.result.likeCount").value(0));

        mockMvc.perform(post(CONTENT_URL + "/like")
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.likedByMe").value(true))
            .andExpect(jsonPath("$.result.likeCount").value(1));

        mockMvc.perform(get(CONTENT_URL + "/like")
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.likedByMe").value(true))
            .andExpect(jsonPath("$.result.likeCount").value(1));
    }

    @Test
    @DisplayName("본인 댓글만 수정할 수 있고 수정된 내용이 조회된다")
    void 본인_댓글만_수정할_수_있고_수정된_내용이_조회된다() throws Exception {
        Long commentId = createComment("수정 전 댓글", null, authorToken);

        mockMvc.perform(post(BASE_URL + "/" + commentId + "/like")
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.likeCount").value(1));

        mockMvc.perform(patch(BASE_URL + "/" + commentId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBody("다른 사용자 수정"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(patch(BASE_URL + "/" + commentId)
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBody("수정 후 댓글"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.id").value(commentId))
            .andExpect(jsonPath("$.result.content").value("수정 후 댓글"))
            .andExpect(jsonPath("$.result.likeCount").value(1))
            .andExpect(jsonPath("$.result.likedByMe").value(false))
            .andExpect(jsonPath("$.result.canEdit").value(true))
            .andExpect(jsonPath("$.result.canDelete").value(true));

        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].id").value(commentId))
            .andExpect(jsonPath("$.result.content[0].content").value("수정 후 댓글"))
            .andExpect(jsonPath("$.result.content[0].likeCount").value(1))
            .andExpect(jsonPath("$.result.content[0].canEdit").value(true))
            .andExpect(jsonPath("$.result.content[0].canDelete").value(true));

        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].canEdit").value(false))
            .andExpect(jsonPath("$.result.content[0].canDelete").value(false));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].canEdit").value(false))
            .andExpect(jsonPath("$.result.content[0].canDelete").value(false));
    }

    @Test
    @DisplayName("댓글 목록은 size와 createdAt 정렬 기준에 맞춰 커서 페이지네이션된다")
    void 댓글_목록은_size와_createdAt_정렬_기준에_맞춰_커서_페이지네이션된다() throws Exception {
        Long firstId = createComment("첫 번째 댓글", null, authorToken);
        Long secondId = createComment("두 번째 댓글", null, authorToken);
        Long thirdId = createComment("세 번째 댓글", null, authorToken);

        mockMvc.perform(get(BASE_URL)
                .queryParam("sort", "createdAt,asc")
                .queryParam("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].id").value(firstId))
            .andExpect(jsonPath("$.result.content[1].id").value(secondId))
            .andExpect(jsonPath("$.result.hasNext").value(true))
            .andExpect(jsonPath("$.result.nextCursor").value(secondId));

        mockMvc.perform(get(BASE_URL)
                .queryParam("sort", "createdAt,asc")
                .queryParam("size", "2")
                .queryParam("cursor", secondId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].id").value(thirdId))
            .andExpect(jsonPath("$.result.hasNext").value(false));

        mockMvc.perform(get(BASE_URL)
                .queryParam("sort", "createdAt,desc")
                .queryParam("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].id").value(thirdId))
            .andExpect(jsonPath("$.result.content[1].id").value(secondId))
            .andExpect(jsonPath("$.result.hasNext").value(true))
            .andExpect(jsonPath("$.result.nextCursor").value(secondId));
    }

    @Test
    @DisplayName("지원하지 않는 댓글 정렬 조건은 400을 반환한다")
    void 지원하지_않는_댓글_정렬_조건은_400을_반환한다() throws Exception {
        createComment("댓글", null, authorToken);

        mockMvc.perform(get(BASE_URL)
                .queryParam("sort", "likeCount,desc"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대댓글이 있는 부모 댓글을 본인이 삭제하면 부모는 삭제 placeholder로 남고 대댓글은 조회된다")
    void 대댓글이_있는_부모_댓글을_본인이_삭제하면_부모는_삭제_placeholder로_남고_대댓글은_조회된다() throws Exception {
        Long parentId = createComment("부모 댓글", null, authorToken);
        Long replyId = createComment("대댓글", parentId, authorToken);

        mockMvc.perform(delete(BASE_URL + "/" + parentId)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].id").value(parentId))
            .andExpect(jsonPath("$.result.content[0].content").value("삭제된 댓글입니다"))
            .andExpect(jsonPath("$.result.content[0].deletionType").value("USER_DELETED"))
            .andExpect(jsonPath("$.result.content[0].canReply").value(false))
            .andExpect(jsonPath("$.result.content[0].likedByMe").value(false))
            .andExpect(jsonPath("$.result.content[0].likeCount").value(0))
            .andExpect(jsonPath("$.result.content[0].author").doesNotExist())
            .andExpect(jsonPath("$.result.content[0].canEdit").value(false))
            .andExpect(jsonPath("$.result.content[0].canDelete").value(false))
            .andExpect(jsonPath("$.result.content[0].replies[0].id").value(replyId))
            .andExpect(jsonPath("$.result.content[0].replies[0].content").value("대댓글"));

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentBody("추가 대댓글", parentId))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("삭제된 부모의 모든 대댓글이 사라지면 부모 댓글도 목록에서 제외된다")
    void 삭제된_부모의_모든_대댓글이_사라지면_부모_댓글도_목록에서_제외된다() throws Exception {
        Long parentId = createComment("부모 댓글", null, authorToken);
        Long replyId = createComment("대댓글", parentId, authorToken);

        mockMvc.perform(delete(BASE_URL + "/" + parentId)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk());

        mockMvc.perform(delete(BASE_URL + "/" + replyId)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content").isEmpty());
    }

    @Test
    @DisplayName("관리자 삭제는 관리자 placeholder로 조회되고 일반 사용자는 관리자 삭제를 호출할 수 없다")
    void 관리자_삭제는_관리자_placeholder로_조회되고_일반_사용자는_관리자_삭제를_호출할_수_없다() throws Exception {
        Long parentId = createComment("부모 댓글", null, authorToken);
        createComment("대댓글", parentId, authorToken);

        mockMvc.perform(delete(BASE_URL + "/" + parentId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(delete(BASE_URL + "/" + parentId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content[0].content").value("관리자에 의해서 삭제된 댓글입니다"))
            .andExpect(jsonPath("$.result.content[0].deletionType").value("ADMIN_DELETED"))
            .andExpect(jsonPath("$.result.content[0].canReply").value(false))
            .andExpect(jsonPath("$.result.content[0].canEdit").value(false))
            .andExpect(jsonPath("$.result.content[0].canDelete").value(false));
    }

    private String mockToken(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
        return token;
    }

    private Long createComment(String content, Long parentCommentId, String token) throws Exception {
        String response = mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentBody(content, parentCommentId))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode result = objectMapper.readTree(response).path("result");
        return result.path("id").asLong();
    }

    private Map<String, Object> commentBody(String content, Long parentCommentId) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("anonymous", false);
        if (parentCommentId != null) {
            body.put("parentCommentId", parentCommentId);
        }
        return body;
    }

    private Map<String, Object> updateBody(String content) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        return body;
    }
}
