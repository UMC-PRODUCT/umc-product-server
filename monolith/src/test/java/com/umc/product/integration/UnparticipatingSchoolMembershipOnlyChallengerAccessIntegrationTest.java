package com.umc.product.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.adapter.in.web.assembler.MemberInfoResponseAssembler;
import com.umc.product.member.application.port.in.query.GetMemberSummaryV2UseCase;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberSummaryV2Info.ChallengerHistoryItem;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemV2Info;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.notification.application.event.FcmNotificationRequestedEvent;
import com.umc.product.notification.application.service.FcmAudienceResolver;
import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.schedule.application.port.in.command.CreateScheduleUseCase;
import com.umc.product.schedule.application.port.in.command.dto.CreateScheduleCommand;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("이 기수 지부가 없는 학교의 수강 없는 소속 챌린저의 기본 기능 접근")
class UnparticipatingSchoolMembershipOnlyChallengerAccessIntegrationTest extends IntegrationTestSupport {

    private static final String MEMBER_TOKEN = "unparticipating-membership-only-token";

    @Autowired private GisuFixture gisuFixture;
    @Autowired private ManageGisuUseCase manageGisuUseCase;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SchoolFixture schoolFixture;
    @Autowired private ChallengerFixture challengerFixture;
    @Autowired private SaveMemberPort saveMemberPort;
    @Autowired private EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired private RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;
    @Autowired private CheckPermissionUseCase checkPermissionUseCase;
    @Autowired private SaveNoticePort saveNoticePort;
    @Autowired private SaveNoticeTargetPort saveNoticeTargetPort;
    @Autowired private GetNoticeUseCase getNoticeUseCase;
    @Autowired private FcmAudienceResolver fcmAudienceResolver;
    @Autowired private SearchMemberUseCase searchMemberUseCase;
    @Autowired private CreateScheduleUseCase createScheduleUseCase;
    @Autowired private MemberInfoResponseAssembler memberInfoResponseAssembler;
    @Autowired private GetMemberSummaryV2UseCase getMemberSummaryV2UseCase;

    private Gisu gisu;
    private Chapter otherChapter;
    private School school;
    private Member member;
    private Challenger challenger;
    private Member learner;
    private Challenger learnerChallenger;
    private List<Long> memberIds = List.of();

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        // 마이그레이션 시드에 활성 기수가 있을 수 있어, 운영 경로로 활성 기수를 바꾼다.
        gisu = gisuFixture.비활성_기수(9502L);
        manageGisuUseCase.updateActiveGisu(gisu.getId());
        otherChapter = chapterFixture.지부(gisu, "참여지부");
        School participatingSchool = schoolFixture.지부에_소속된_학교("참여학교", otherChapter);
        school = schoolFixture.학교("미참여학교");
        member = saveMember("프덕", "product@test.com", school.getId());
        challenger = challengerFixture.수강_없는_소속(member.getId(), gisu.getId());
        learner = saveMember("수강생", "learner@test.com", participatingSchool.getId());
        learnerChallenger = challengerFixture.웹(learner.getId(), gisu.getId());
        memberIds = List.of(member.getId(), learner.getId());
        // 테이블을 비울 때 회원 ID가 다시 매겨지므로, 같은 ID로 남은 다른 테스트의 권한 캐시를 지운다.
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);

        given(jwtTokenProvider.validateAccessToken(MEMBER_TOKEN)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(MEMBER_TOKEN)).willReturn(member.getId());
        given(jwtTokenProvider.getRolesFromAccessToken(MEMBER_TOKEN)).willReturn(List.of());
    }

    @AfterEach
    void evictAuthorityCache() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);
    }

    @Test
    @DisplayName("권한 주체는 기수를 담고 지부와 파트는 비어 있다")
    void 권한_주체_로드() {
        // When
        SubjectAttributes subject = checkPermissionUseCase.loadSubject(member.getId());

        // Then
        assertThat(subject.gisuChallengerInfos())
            .extracting(GisuChallengerInfo::gisuId, GisuChallengerInfo::chapterId,
                GisuChallengerInfo::part, GisuChallengerInfo::challengerId)
            .containsExactly(tuple(gisu.getId(), null, null, challenger.getId()));
        assertThat(subject.roleAttributes()).isEmpty();
    }

    @Test
    @DisplayName("기수 전체 공지와 본인 학교 공지는 읽을 수 있고, 지부 공지는 읽을 수 없다")
    void 공지_읽기() {
        // Given
        Long gisuNoticeId = saveNotice(null, null);
        Long schoolNoticeId = saveNotice(null, school.getId());
        Long chapterNoticeId = saveNotice(otherChapter.getId(), null);

        // When / Then
        assertThat(canReadNotice(gisuNoticeId)).isTrue();
        assertThat(canReadNotice(schoolNoticeId)).isTrue();
        assertThat(canReadNotice(chapterNoticeId)).isFalse();
    }

    @Test
    @DisplayName("기수 공지 목록에 기수 전체 공지가 보인다")
    void 공지_목록() throws Exception {
        // Given
        Long gisuNoticeId = saveNotice(null, null);
        Long chapterNoticeId = saveNotice(otherChapter.getId(), null);

        // When
        String body = mockMvc.perform(get("/api/v1/notices")
                .header("Authorization", "Bearer " + MEMBER_TOKEN)
                .param("gisuId", gisu.getId().toString())
                .param("noticeTab", NoticeTab.CHALLENGER.name()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // Then
        List<Long> listedIds = objectMapper.readTree(body).at("/result/content").findValues("id").stream()
            .map(JsonNode::asLong)
            .toList();
        assertThat(listedIds).contains(gisuNoticeId).doesNotContain(chapterNoticeId);
    }

    @Test
    @DisplayName("기수 전체 공지의 읽음 현황과 통계에 지부 없이 포함된다")
    void 공지_읽음_현황() {
        // Given
        Long gisuNoticeId = saveNotice(null, null);

        // When
        List<NoticeReadStatusInfo> unread = getNoticeUseCase.getReadStatus(new GetNoticeStatusQuery(
            null, gisuNoticeId, NoticeReadStatusFilterType.ALL, null, NoticeReadStatus.UNREAD)).content();
        int totalCount = getNoticeUseCase.getReadStatistics(gisuNoticeId).totalCount();

        // Then
        assertThat(unread)
            .extracting(NoticeReadStatusInfo::challengerId, NoticeReadStatusInfo::chapterId)
            .containsExactlyInAnyOrder(
                tuple(challenger.getId(), null),
                tuple(learnerChallenger.getId(), otherChapter.getId()));
        assertThat(totalCount).isEqualTo(2);
    }

    @Test
    @DisplayName("기수 대상 푸시 알림 수신자에 포함되고, 지부 대상 푸시에는 빠진다")
    void 푸시_수신자() {
        // When
        List<Long> gisuRecipients = fcmAudienceResolver.resolve(pushEvent(null));
        List<Long> chapterRecipients = fcmAudienceResolver.resolve(pushEvent(otherChapter.getId()));

        // Then
        assertThat(gisuRecipients).containsExactlyInAnyOrder(member.getId(), learner.getId());
        assertThat(chapterRecipients).containsExactly(learner.getId());
    }

    @Test
    @DisplayName("회원 검색 v1·v2 결과에 이 챌린저가 파트 없이 나온다")
    void 회원_검색() {
        // Given
        SearchMemberQuery query = new SearchMemberQuery(null, gisu.getId(), null, null, null);
        PageRequest pageable = PageRequest.of(0, 20);

        // When
        List<SearchMemberItemInfo> v1Items = searchMemberUseCase.searchBy(query, member.getId(), pageable)
            .page().getContent();
        List<SearchMemberItemV2Info> v2Items = searchMemberUseCase.searchByV2(query, member.getId(), pageable)
            .page().getContent();

        // Then
        assertThat(v1Items)
            .extracting(SearchMemberItemInfo::memberId, SearchMemberItemInfo::challengerId,
                SearchMemberItemInfo::part)
            .contains(tuple(member.getId(), challenger.getId(), null));
        assertThat(v1Items).extracting(SearchMemberItemInfo::memberId).contains(learner.getId());
        assertThat(v2Items)
            .extracting(SearchMemberItemV2Info::memberId,
                item -> item.primaryChallenger().challengerId(), item -> item.primaryChallenger().part())
            .contains(tuple(member.getId(), challenger.getId(), null));
    }

    @Test
    @DisplayName("커뮤니티 게시글을 작성하면 이 챌린저가 작성자가 된다")
    void 커뮤니티_게시글_작성() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/v1/posts")
                .header("Authorization", "Bearer " + MEMBER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "프로덕트팀 인사", "content": "잘 부탁드립니다.", "category": "FREE"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.authorChallengerId").value(challenger.getId()));
    }

    @Test
    @DisplayName("출석 없는 일정을 만들 수 있다")
    void 일정_생성() {
        // When
        Long scheduleId = createScheduleUseCase.create(CreateScheduleCommand.builder()
            .name("프로덕트팀 회의")
            .tags(Set.of(ScheduleTag.MEETING))
            .authorMemberId(member.getId())
            .startsAt(gisu.getStartAt().plus(Duration.ofDays(1)))
            .endsAt(gisu.getStartAt().plus(Duration.ofDays(1)).plus(Duration.ofHours(2)))
            .participantMemberIds(Set.of())
            .build());

        // Then
        assertThat(scheduleId).isNotNull();
    }

    @Test
    @DisplayName("내 정보 v1·v2에 이 기수의 챌린저 ID가 지부 없이 담긴다")
    void 내_정보_조회() {
        // When
        List<ChallengerInfoResponse> v1Records = memberInfoResponseAssembler.fromMemberId(member.getId())
            .challengerRecords();
        List<ChallengerHistoryItem> v2History = getMemberSummaryV2UseCase.getSummaryByMemberId(member.getId())
            .challengerHistory();

        // Then
        assertThat(v1Records)
            .extracting(ChallengerInfoResponse::gisuId, ChallengerInfoResponse::challengerId,
                ChallengerInfoResponse::chapterId)
            .containsExactly(tuple(gisu.getId(), challenger.getId(), null));
        assertThat(v2History)
            .extracting(ChallengerHistoryItem::gisuId, ChallengerHistoryItem::challengerId,
                ChallengerHistoryItem::chapterId)
            .containsExactly(tuple(gisu.getId(), challenger.getId(), null));
    }

    private Member saveMember(String name, String email, Long schoolId) {
        return saveMemberPort.save(Member.create(name, name, email, schoolId, null));
    }

    private Long saveNotice(Long targetChapterId, Long targetSchoolId) {
        Notice notice = saveNoticePort.save(Notice.create("공지", "내용", learner.getId(), false, false));
        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(notice.getId())
            .targetGisuId(gisu.getId())
            .targetChapterId(targetChapterId)
            .targetSchoolId(targetSchoolId)
            .targetChallengerPart(List.<ChallengerPart>of())
            .targetNoticeTab(NoticeTab.CHALLENGER)
            .build());
        return notice.getId();
    }

    private boolean canReadNotice(Long noticeId) {
        return checkPermissionUseCase.check(member.getId(),
            ResourcePermission.of(ResourceType.NOTICE, noticeId, PermissionType.READ));
    }

    private FcmNotificationRequestedEvent pushEvent(Long targetChapterId) {
        return new FcmNotificationRequestedEvent(null, null, null, learner.getId(), List.of(),
            gisu.getId(), targetChapterId, null, Set.of(), "제목", "본문", null, null, null);
    }
}
