package com.umc.product.integration;

import static com.umc.product.integration.MembershipOnlyChallengerAccessIntegrationTest.소속_학교.지부_없는_학교;
import static com.umc.product.integration.MembershipOnlyChallengerAccessIntegrationTest.소속_학교.지부_있는_학교;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
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
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.notice.application.port.in.command.ManageNoticeReadUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
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
import com.umc.product.schedule.application.port.in.query.GetScheduleCapabilitiesUseCase;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleCapabilitiesInfo;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("수강 없는 소속 챌린저의 기본 기능 접근")
class MembershipOnlyChallengerAccessIntegrationTest extends IntegrationTestSupport {

    enum 소속_학교 { 지부_있는_학교, 지부_없는_학교 }

    private record MembershipOnly(Member member, Challenger challenger, String token, Long chapterId) {
    }

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
    @Autowired private ManageNoticeReadUseCase manageNoticeReadUseCase;
    @Autowired private LoadNoticeReadPort loadNoticeReadPort;
    @Autowired private GetNoticeUseCase getNoticeUseCase;
    @Autowired private FcmAudienceResolver fcmAudienceResolver;
    @Autowired private SearchMemberUseCase searchMemberUseCase;
    @Autowired private GetScheduleCapabilitiesUseCase getScheduleCapabilitiesUseCase;
    @Autowired private CreateScheduleUseCase createScheduleUseCase;
    @Autowired private GetCurriculumUseCase getCurriculumUseCase;
    @Autowired private MemberInfoResponseAssembler memberInfoResponseAssembler;
    @Autowired private GetMemberSummaryV2UseCase getMemberSummaryV2UseCase;

    private Gisu gisu;
    private Chapter chapter;
    private School unassignedSchool;
    private Map<소속_학교, MembershipOnly> membershipOnlyBySchool = Map.of();
    private Member learner;
    private Challenger learnerChallenger;
    private Member outsider;
    private List<Long> memberIds = List.of();

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        // 마이그레이션 시드에 활성 기수가 있을 수 있어, 운영 경로로 활성 기수를 바꾼다.
        gisu = gisuFixture.비활성_기수(9501L);
        manageGisuUseCase.updateActiveGisu(gisu.getId());
        chapter = chapterFixture.지부(gisu, "소속지부");
        School assignedSchool = schoolFixture.지부에_소속된_학교("소속학교", chapter);
        unassignedSchool = schoolFixture.학교("미참여학교");
        membershipOnlyBySchool = Map.of(
            지부_있는_학교, saveMembershipOnly("프덕", "product@test.com", assignedSchool,
                "membership-only-token", chapter.getId()),
            지부_없는_학교, saveMembershipOnly("미참여프덕", "unassigned-product@test.com", unassignedSchool,
                "unassigned-membership-only-token", null));
        learner = saveMember("수강생", "learner@test.com", assignedSchool.getId());
        learnerChallenger = challengerFixture.웹(learner.getId(), gisu.getId());
        outsider = saveMember("외부인", "outsider@test.com", assignedSchool.getId());
        memberIds = List.of(membershipOnly(지부_있는_학교).member().getId(),
            membershipOnly(지부_없는_학교).member().getId(), learner.getId(), outsider.getId());
        // 테이블을 비울 때 회원 ID가 다시 매겨지므로, 같은 ID로 남은 다른 테스트의 권한 캐시를 지운다.
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);
    }

    @AfterEach
    void evictAuthorityCache() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(소속_학교.class)
    @DisplayName("권한 주체는 기수와 학교의 지부를 담고, 파트와 역할은 비어 있다")
    void 권한_주체_로드(소속_학교 학교) {
        // Given
        MembershipOnly subjectMember = membershipOnly(학교);

        // When
        SubjectAttributes subject = checkPermissionUseCase.loadSubject(subjectMember.member().getId());

        // Then
        assertThat(subject.gisuChallengerInfos())
            .extracting(GisuChallengerInfo::gisuId, GisuChallengerInfo::chapterId,
                GisuChallengerInfo::part, GisuChallengerInfo::challengerId)
            .containsExactly(tuple(gisu.getId(), subjectMember.chapterId(), null,
                subjectMember.challenger().getId()));
        assertThat(subject.roleAttributes()).isEmpty();
        assertThat(subject.systemRoles()).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(소속_학교.class)
    @DisplayName("기수 전체 공지는 읽고 읽음 처리할 수 있고, 파트 한정 공지는 읽을 수 없다")
    void 공지_읽기(소속_학교 학교) {
        // Given
        MembershipOnly reader = membershipOnly(학교);
        Long gisuNoticeId = saveNotice(null, null, List.of());
        Long partNoticeId = saveNotice(null, null, List.of(ChallengerPart.WEB_PRODUCT_ENGINEER));

        // When
        boolean canReadGisuNotice = canReadNotice(reader, gisuNoticeId);
        boolean canReadPartNotice = canReadNotice(reader, partNoticeId);
        manageNoticeReadUseCase.recordRead(gisuNoticeId, reader.member().getId());

        // Then
        assertThat(canReadGisuNotice).isTrue();
        assertThat(canReadPartNotice).isFalse();
        assertThat(loadNoticeReadPort.existsRead(gisuNoticeId, reader.challenger().getId())).isTrue();
    }

    @Test
    @DisplayName("지부 없는 학교의 챌린저는 본인 학교 공지를 읽고, 지부 공지는 읽을 수 없고 목록에서도 보이지 않는다")
    void 지부_없는_학교의_학교_공지와_지부_공지() throws Exception {
        // Given
        MembershipOnly reader = membershipOnly(지부_없는_학교);
        Long schoolNoticeId = saveNotice(null, unassignedSchool.getId(), List.of());
        Long chapterNoticeId = saveNotice(chapter.getId(), null, List.of());

        // When
        boolean canReadSchoolNotice = canReadNotice(reader, schoolNoticeId);
        boolean canReadChapterNotice = canReadNotice(reader, chapterNoticeId);
        List<Long> listedIds = listChallengerNoticeIds(reader);

        // Then
        assertThat(canReadSchoolNotice).isTrue();
        assertThat(canReadChapterNotice).isFalse();
        assertThat(listedIds).doesNotContain(chapterNoticeId);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(소속_학교.class)
    @DisplayName("기수 공지 목록에는 기수 전체 공지만 보이고 파트 한정 공지는 보이지 않는다")
    void 공지_목록(소속_학교 학교) throws Exception {
        // Given
        Long gisuNoticeId = saveNotice(null, null, List.of());
        Long partNoticeId = saveNotice(null, null, List.of(ChallengerPart.WEB_PRODUCT_ENGINEER));

        // When
        List<Long> listedIds = listChallengerNoticeIds(membershipOnly(학교));

        // Then
        assertThat(listedIds).contains(gisuNoticeId).doesNotContain(partNoticeId);
    }

    @Test
    @DisplayName("기수 전체 공지의 읽음 현황과 통계에 지부 없는 학교의 챌린저가 지부 없이 포함된다")
    void 공지_읽음_현황() {
        // Given
        Long gisuNoticeId = saveNotice(null, null, List.of());

        // When
        List<NoticeReadStatusInfo> unread = getNoticeUseCase.getReadStatus(new GetNoticeStatusQuery(
            null, gisuNoticeId, NoticeReadStatusFilterType.ALL, null, NoticeReadStatus.UNREAD)).content();
        int totalCount = getNoticeUseCase.getReadStatistics(gisuNoticeId).totalCount();

        // Then
        assertThat(unread)
            .extracting(NoticeReadStatusInfo::challengerId, NoticeReadStatusInfo::chapterId)
            .containsExactlyInAnyOrder(
                tuple(membershipOnly(지부_있는_학교).challenger().getId(), chapter.getId()),
                tuple(membershipOnly(지부_없는_학교).challenger().getId(), null),
                tuple(learnerChallenger.getId(), chapter.getId()));
        assertThat(totalCount).isEqualTo(3);
    }

    @Test
    @DisplayName("기수 대상 푸시 알림 수신자에 포함되고, 지부 대상 푸시에는 지부 없는 학교의 챌린저가 빠진다")
    void 푸시_수신자() {
        // Given
        Long assignedMemberId = membershipOnly(지부_있는_학교).member().getId();
        Long unassignedMemberId = membershipOnly(지부_없는_학교).member().getId();

        // When
        List<Long> gisuRecipients = fcmAudienceResolver.resolve(pushEvent(null));
        List<Long> chapterRecipients = fcmAudienceResolver.resolve(pushEvent(chapter.getId()));

        // Then
        assertThat(gisuRecipients).containsExactlyInAnyOrder(assignedMemberId, unassignedMemberId, learner.getId());
        assertThat(chapterRecipients).containsExactlyInAnyOrder(assignedMemberId, learner.getId());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(소속_학교.class)
    @DisplayName("회원 검색 v1·v2 결과에 이 챌린저가 파트 없이 나온다")
    void 회원_검색(소속_학교 학교) {
        // Given
        MembershipOnly searcher = membershipOnly(학교);
        SearchMemberQuery query = new SearchMemberQuery(null, gisu.getId(), null, null, null);
        PageRequest pageable = PageRequest.of(0, 20);

        // When
        List<SearchMemberItemInfo> v1Items = searchMemberUseCase.searchBy(query, searcher.member().getId(), pageable)
            .page().getContent();
        List<SearchMemberItemV2Info> v2Items = searchMemberUseCase.searchByV2(query, searcher.member().getId(),
            pageable).page().getContent();

        // Then
        assertThat(v1Items)
            .extracting(SearchMemberItemInfo::memberId, SearchMemberItemInfo::challengerId,
                SearchMemberItemInfo::part)
            .contains(tuple(searcher.member().getId(), searcher.challenger().getId(), null));
        assertThat(v1Items).extracting(SearchMemberItemInfo::memberId).contains(learner.getId());
        assertThat(v2Items)
            .extracting(SearchMemberItemV2Info::memberId,
                item -> item.primaryChallenger().challengerId(), item -> item.primaryChallenger().part())
            .contains(tuple(searcher.member().getId(), searcher.challenger().getId(), null));
    }

    @Test
    @DisplayName("챌린저 이력이 없는 회원은 검색할 수 없다")
    void 챌린저_이력_없는_회원_검색_거부() {
        // Given
        SearchMemberQuery query = new SearchMemberQuery(null, null, null, null, null);
        PageRequest pageable = PageRequest.of(0, 20);

        // When / Then
        assertThatThrownBy(() -> searchMemberUseCase.searchBy(query, outsider.getId(), pageable))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.MEMBER_SEARCH_ACCESS_DENIED);
        assertThatThrownBy(() -> searchMemberUseCase.searchByV2(query, outsider.getId(), pageable))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode")
            .isEqualTo(MemberErrorCode.MEMBER_SEARCH_ACCESS_DENIED);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(소속_학교.class)
    @DisplayName("커뮤니티 게시글을 작성하면 이 챌린저가 작성자가 된다")
    void 커뮤니티_게시글_작성(소속_학교 학교) throws Exception {
        // Given
        MembershipOnly author = membershipOnly(학교);

        // When / Then
        mockMvc.perform(post("/api/v1/posts")
                .header("Authorization", "Bearer " + author.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "프로덕트팀 인사", "content": "잘 부탁드립니다.", "category": "FREE"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.authorChallengerId").value(author.challenger().getId()));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(소속_학교.class)
    @DisplayName("일정 권한은 역할 없는 수강생과 같고, 출석 없는 일정을 만들 수 있다")
    void 일정_생성(소속_학교 학교) {
        // Given
        Long memberId = membershipOnly(학교).member().getId();
        ScheduleCapabilitiesInfo learnerCapabilities = getScheduleCapabilitiesUseCase.getCapabilities(learner.getId());

        // When
        ScheduleCapabilitiesInfo capabilities = getScheduleCapabilitiesUseCase.getCapabilities(memberId);
        Long scheduleId = createScheduleUseCase.create(CreateScheduleCommand.builder()
            .name("프로덕트팀 회의")
            .tags(Set.of(ScheduleTag.MEETING))
            .authorMemberId(memberId)
            .startsAt(gisu.getStartAt().plus(Duration.ofDays(1)))
            .endsAt(gisu.getStartAt().plus(Duration.ofDays(1)).plus(Duration.ofHours(2)))
            .participantMemberIds(Set.of())
            .build());

        // Then
        assertThat(capabilities).isEqualTo(learnerCapabilities);
        assertThat(capabilities.canCreateSchedule()).isTrue();
        assertThat(scheduleId).isNotNull();
    }

    @Test
    @DisplayName("파트를 지정하지 않은 커리큘럼 조회는 트랙 필요 오류가 난다")
    void 커리큘럼_조회() {
        // Given
        Long memberId = membershipOnly(지부_있는_학교).member().getId();

        // When / Then
        assertThatThrownBy(() -> getCurriculumUseCase.getMyProgress(memberId, gisu.getId()))
            .isInstanceOf(CurriculumDomainException.class)
            .extracting("baseCode")
            .isEqualTo(CurriculumErrorCode.CURRICULUM_TRACK_REQUIRED);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(소속_학교.class)
    @DisplayName("내 정보 v1 challengerRecords와 v2 challengerHistory에 이 기수의 챌린저 ID와 학교의 지부가 담긴다")
    void 내_정보_조회(소속_학교 학교) {
        // Given
        MembershipOnly me = membershipOnly(학교);

        // When
        List<ChallengerInfoResponse> v1Records = memberInfoResponseAssembler.fromMemberId(me.member().getId())
            .challengerRecords();
        List<ChallengerHistoryItem> v2History = getMemberSummaryV2UseCase.getSummaryByMemberId(me.member().getId())
            .challengerHistory();

        // Then
        assertThat(v1Records)
            .extracting(ChallengerInfoResponse::gisuId, ChallengerInfoResponse::challengerId,
                ChallengerInfoResponse::chapterId)
            .containsExactly(tuple(gisu.getId(), me.challenger().getId(), me.chapterId()));
        assertThat(v2History)
            .extracting(ChallengerHistoryItem::gisuId, ChallengerHistoryItem::challengerId,
                ChallengerHistoryItem::chapterId)
            .containsExactly(tuple(gisu.getId(), me.challenger().getId(), me.chapterId()));
    }

    private MembershipOnly saveMembershipOnly(String name, String email, School school, String token,
        Long chapterId) {
        Member member = saveMember(name, email, school.getId());
        Challenger challenger = challengerFixture.수강_없는_소속(member.getId(), gisu.getId());
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(member.getId());
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
        return new MembershipOnly(member, challenger, token, chapterId);
    }

    private MembershipOnly membershipOnly(소속_학교 학교) {
        return membershipOnlyBySchool.get(학교);
    }

    private Member saveMember(String name, String email, Long schoolId) {
        return saveMemberPort.save(Member.create(name, name, email, schoolId, null));
    }

    private Long saveNotice(Long targetChapterId, Long targetSchoolId, List<ChallengerPart> targetParts) {
        Notice notice = saveNoticePort.save(Notice.create("공지", "내용", learner.getId(), false, false));
        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(notice.getId())
            .targetGisuId(gisu.getId())
            .targetChapterId(targetChapterId)
            .targetSchoolId(targetSchoolId)
            .targetChallengerPart(targetParts)
            .targetNoticeTab(NoticeTab.CHALLENGER)
            .build());
        return notice.getId();
    }

    private boolean canReadNotice(MembershipOnly reader, Long noticeId) {
        return checkPermissionUseCase.check(reader.member().getId(),
            ResourcePermission.of(ResourceType.NOTICE, noticeId, PermissionType.READ));
    }

    private List<Long> listChallengerNoticeIds(MembershipOnly reader) throws Exception {
        String body = mockMvc.perform(get("/api/v1/notices")
                .header("Authorization", "Bearer " + reader.token())
                .param("gisuId", gisu.getId().toString())
                .param("noticeTab", NoticeTab.CHALLENGER.name()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/result/content").findValues("id").stream()
            .map(JsonNode::asLong)
            .toList();
    }

    private FcmNotificationRequestedEvent pushEvent(Long targetChapterId) {
        return new FcmNotificationRequestedEvent(null, null, null, learner.getId(), List.of(),
            gisu.getId(), targetChapterId, null, Set.of(), "제목", "본문", null, null, null);
    }
}
