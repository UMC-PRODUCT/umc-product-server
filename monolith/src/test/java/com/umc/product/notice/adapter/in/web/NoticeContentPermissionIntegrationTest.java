package com.umc.product.notice.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.form.application.port.out.LoadFormPort;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.adapter.out.persistence.MemberSystemRoleJpaRepository;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeVoteRequest;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.out.LoadNoticeImagePort;
import com.umc.product.notice.application.port.out.LoadNoticeLinkPort;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.application.port.out.SaveNoticeImagePort;
import com.umc.product.notice.application.port.out.SaveNoticeLinkPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeImage;
import com.umc.product.notice.domain.NoticeLink;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.NoticeVote;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("공지 첨부 변경 권한 통합 테스트")
class NoticeContentPermissionIntegrationTest extends IntegrationTestSupport {

    @Autowired
    SchoolFixture schoolFixture;
    @Autowired
    SaveMemberPort saveMemberPort;
    @Autowired
    MemberSystemRoleJpaRepository memberSystemRoleJpaRepository;
    @Autowired
    EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired
    RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;
    @Autowired
    SaveNoticePort saveNoticePort;
    @Autowired
    SaveNoticeTargetPort saveNoticeTargetPort;
    @Autowired
    SaveNoticeImagePort saveNoticeImagePort;
    @Autowired
    SaveNoticeLinkPort saveNoticeLinkPort;
    @Autowired
    LoadNoticePort loadNoticePort;
    @Autowired
    LoadNoticeImagePort loadNoticeImagePort;
    @Autowired
    LoadNoticeLinkPort loadNoticeLinkPort;
    @Autowired
    LoadNoticeVotePort loadNoticeVotePort;
    @Autowired
    LoadFormPort loadFormPort;
    @Autowired
    ManageNoticeContentUseCase manageNoticeContentUseCase;
    @Autowired
    Clock clock;

    private Long authorMemberId;
    private Long noticeId;
    private List<Long> memberIds = List.of();

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        Long schoolId = schoolFixture.학교("공지 테스트 학교").getId();
        authorMemberId = saveMember("notice-author", schoolId);
        Long superAdminMemberId = saveMember("notice-super-admin", schoolId);
        Long otherMemberId = saveMember("notice-other", schoolId);
        memberSystemRoleJpaRepository.save(MemberSystemRole.create(superAdminMemberId, MemberSystemRoleType.SUPER_ADMIN));
        memberIds = List.of(authorMemberId, superAdminMemberId, otherMemberId);
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);

        setUpToken("author-token", authorMemberId);
        setUpToken("super-admin-token", superAdminMemberId);
        setUpToken("other-token", otherMemberId);

        Notice notice = saveNoticePort.save(Notice.create("제목", "내용", authorMemberId, false, false));
        noticeId = notice.getId();
        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(noticeId)
            .targetGisuId(9L)
            .targetChallengerPart(List.of())
            .targetNoticeTab(NoticeTab.CHALLENGER)
            .build());
        saveNoticeImagePort.saveAllImages(List.of(NoticeImage.create("existing-image", notice, 0)));
        saveNoticeLinkPort.saveAllLinks(List.of(NoticeLink.create("https://example.com/old", notice, 0)));
    }

    @AfterEach
    void clearAuthorityCache() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    @DisplayName("작성자는 자신의 공지 이미지·링크·투표를 변경할 수 있다")
    void 작성자_첨부_변경_허용(Operation operation) throws Exception {
        // Given
        Long formId = prepareVoteForDeletion(operation);

        // When / Then
        mockMvc.perform(request(operation, "author-token"))
            .andExpect(status().isOk());
        assertChangedContents(operation, formId);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    @DisplayName("SUPER_ADMIN은 다른 작성자의 공지 이미지·링크·투표를 변경할 수 있다")
    void SUPER_ADMIN_첨부_변경_허용(Operation operation) throws Exception {
        // Given
        Long formId = prepareVoteForDeletion(operation);

        // When / Then
        mockMvc.perform(request(operation, "super-admin-token"))
            .andExpect(status().isOk());
        assertChangedContents(operation, formId);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    @DisplayName("다른 일반 계정은 403을 받고 첨부와 수정 일시는 유지된다")
    void 다른_일반_계정_첨부_변경_거부(Operation operation) throws Exception {
        // Given
        Long formId = prepareVoteForDeletion(operation);
        Instant updatedAt = loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt();

        // When / Then
        mockMvc.perform(request(operation, "other-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("NOTICE-0008"));

        assertThat(loadNoticeImagePort.findImagesByNoticeId(noticeId))
            .extracting(NoticeImage::getImageId).containsExactly("existing-image");
        assertThat(loadNoticeLinkPort.findLinksByNoticeId(noticeId))
            .extracting(NoticeLink::getLink).containsExactly("https://example.com/old");
        assertThat(loadNoticeVotePort.findVoteByNoticeId(noticeId).map(NoticeVote::getVoteId))
            .isEqualTo(Optional.ofNullable(formId));
        if (formId != null) {
            assertThat(loadFormPort.findById(formId)).isPresent();
        }
        assertThat(loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt()).isEqualTo(updatedAt);
    }

    private void setUpToken(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }

    private Long saveMember(String name, Long schoolId) {
        return saveMemberPort.save(Member.create(name, name, name + "@test.com", schoolId, null)).getId();
    }

    private AddNoticeVoteRequest voteRequest() {
        Instant now = clock.instant();
        return new AddNoticeVoteRequest("투표", true, false,
            now.minusSeconds(60), now.plusSeconds(3600), List.of("선택지 1", "선택지 2"));
    }

    private Long prepareVoteForDeletion(Operation operation) {
        if (operation != Operation.DELETE_VOTE) {
            return null;
        }
        return manageNoticeContentUseCase.addVote(voteRequest().toCommand(authorMemberId), noticeId).voteId();
    }

    private MockHttpServletRequestBuilder request(Operation operation, String token) throws Exception {
        String path = "/api/v1/notices/" + noticeId;
        MockHttpServletRequestBuilder builder = switch (operation) {
            case ADD_IMAGES -> post(path + "/images")
                .content(objectMapper.writeValueAsString(Map.of("imageIds", List.of("new-image"))));
            case ADD_LINKS -> post(path + "/links")
                .content(objectMapper.writeValueAsString(Map.of("links", List.of("https://example.com/new"))));
            case ADD_VOTE -> post(path + "/votes").content(objectMapper.writeValueAsString(voteRequest()));
            case REPLACE_IMAGES -> patch(path + "/images")
                .content(objectMapper.writeValueAsString(Map.of("imageIds", List.of("new-image"))));
            case REPLACE_LINKS -> patch(path + "/links")
                .content(objectMapper.writeValueAsString(Map.of("links", List.of("https://example.com/new"))));
            case DELETE_VOTE -> delete(path + "/vote");
        };
        return builder.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    private void assertChangedContents(Operation operation, Long deletedFormId) {
        List<String> expectedImages = switch (operation) {
            case ADD_IMAGES -> List.of("existing-image", "new-image");
            case REPLACE_IMAGES -> List.of("new-image");
            default -> List.of("existing-image");
        };
        List<String> expectedLinks = switch (operation) {
            case ADD_LINKS -> List.of("https://example.com/old", "https://example.com/new");
            case REPLACE_LINKS -> List.of("https://example.com/new");
            default -> List.of("https://example.com/old");
        };
        assertThat(loadNoticeImagePort.findImagesByNoticeId(noticeId))
            .extracting(NoticeImage::getImageId).containsExactlyElementsOf(expectedImages);
        assertThat(loadNoticeLinkPort.findLinksByNoticeId(noticeId))
            .extracting(NoticeLink::getLink).containsExactlyElementsOf(expectedLinks);

        if (operation == Operation.ADD_VOTE) {
            NoticeVote vote = loadNoticeVotePort.findVoteByNoticeId(noticeId).orElseThrow();
            assertThat(loadFormPort.findById(vote.getVoteId())).isPresent();
        } else {
            assertThat(loadNoticeVotePort.findVoteByNoticeId(noticeId)).isEmpty();
        }
        if (operation == Operation.DELETE_VOTE) {
            assertThat(loadFormPort.findById(deletedFormId)).isEmpty();
        }
    }

    enum Operation {
        ADD_IMAGES,
        ADD_LINKS,
        ADD_VOTE,
        REPLACE_IMAGES,
        REPLACE_LINKS,
        DELETE_VOTE
    }
}
