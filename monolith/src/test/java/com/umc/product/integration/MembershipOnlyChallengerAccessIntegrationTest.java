package com.umc.product.integration;

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
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.enums.NoticeTab;
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

    private static final String MEMBER_TOKEN = "membership-only-token";

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
    @Autowired private SearchMemberUseCase searchMemberUseCase;
    @Autowired private GetScheduleCapabilitiesUseCase getScheduleCapabilitiesUseCase;
    @Autowired private CreateScheduleUseCase createScheduleUseCase;
    @Autowired private GetCurriculumUseCase getCurriculumUseCase;
    @Autowired private MemberInfoResponseAssembler memberInfoResponseAssembler;
    @Autowired private GetMemberSummaryV2UseCase getMemberSummaryV2UseCase;

    private Gisu gisu;
    private Chapter chapter;
    private Member member;
    private Challenger challenger;
    private Member learner;
    private Member outsider;
    private List<Long> memberIds = List.of();

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        // 마이그레이션 시드에 활성 기수가 있을 수 있어, 운영 경로로 활성 기수를 바꾼다.
        gisu = gisuFixture.비활성_기수(9501L);
        manageGisuUseCase.updateActiveGisu(gisu.getId());
        chapter = chapterFixture.지부(gisu, "소속지부");
        School school = schoolFixture.지부에_소속된_학교("소속학교", chapter);
        member = saveMember("프덕", "product@test.com", school.getId());
        challenger = challengerFixture.수강_없는_소속(member.getId(), gisu.getId());
        learner = saveMember("수강생", "learner@test.com", school.getId());
        challengerFixture.웹(learner.getId(), gisu.getId());
        outsider = saveMember("외부인", "outsider@test.com", school.getId());
        memberIds = List.of(member.getId(), learner.getId(), outsider.getId());
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
    @DisplayName("권한 주체는 기수와 학교의 지부를 담고, 파트는 비어 있다")
    void 권한_주체_로드() {
        // When
        SubjectAttributes subject = checkPermissionUseCase.loadSubject(member.getId());

        // Then
        assertThat(subject.gisuChallengerInfos())
            .extracting(GisuChallengerInfo::gisuId, GisuChallengerInfo::chapterId,
                GisuChallengerInfo::part, GisuChallengerInfo::challengerId)
            .containsExactly(tuple(gisu.getId(), chapter.getId(), null, challenger.getId()));
        assertThat(subject.roleAttributes()).isEmpty();
        assertThat(subject.systemRoles()).isEmpty();
    }

    @Test
    @DisplayName("기수 전체 공지는 읽고 읽음 처리할 수 있고, 파트 한정 공지는 읽을 수 없다")
    void 공지_읽기() {
        // Given
        Long gisuNoticeId = saveNotice(List.of());
        Long partNoticeId = saveNotice(List.of(ChallengerPart.WEB_PRODUCT_ENGINEER));

        // When
        boolean canReadGisuNotice = canReadNotice(gisuNoticeId);
        boolean canReadPartNotice = canReadNotice(partNoticeId);
        manageNoticeReadUseCase.recordRead(gisuNoticeId, member.getId());

        // Then
        assertThat(canReadGisuNotice).isTrue();
        assertThat(canReadPartNotice).isFalse();
        assertThat(loadNoticeReadPort.existsRead(gisuNoticeId, challenger.getId())).isTrue();
    }

    @Test
    @DisplayName("기수 공지 목록에는 기수 전체 공지만 보이고 파트 한정 공지는 보이지 않는다")
    void 공지_목록() throws Exception {
        // Given
        Long gisuNoticeId = saveNotice(List.of());
        Long partNoticeId = saveNotice(List.of(ChallengerPart.WEB_PRODUCT_ENGINEER));

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
        assertThat(listedIds).contains(gisuNoticeId).doesNotContain(partNoticeId);
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
    @DisplayName("일정 권한은 역할 없는 수강생과 같고, 출석 없는 일정을 만들 수 있다")
    void 일정_생성() {
        // Given
        ScheduleCapabilitiesInfo learnerCapabilities = getScheduleCapabilitiesUseCase.getCapabilities(learner.getId());

        // When
        ScheduleCapabilitiesInfo capabilities = getScheduleCapabilitiesUseCase.getCapabilities(member.getId());
        Long scheduleId = createScheduleUseCase.create(CreateScheduleCommand.builder()
            .name("프로덕트팀 회의")
            .tags(Set.of(ScheduleTag.MEETING))
            .authorMemberId(member.getId())
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
        // When / Then
        assertThatThrownBy(() -> getCurriculumUseCase.getMyProgress(member.getId(), gisu.getId()))
            .isInstanceOf(CurriculumDomainException.class)
            .extracting("baseCode")
            .isEqualTo(CurriculumErrorCode.CURRICULUM_TRACK_REQUIRED);
    }

    @Test
    @DisplayName("내 정보 v1 challengerRecords와 v2 challengerHistory에 이 기수의 챌린저 ID가 담긴다")
    void 내_정보_조회() {
        // When
        List<ChallengerInfoResponse> v1Records = memberInfoResponseAssembler.fromMemberId(member.getId())
            .challengerRecords();
        List<ChallengerHistoryItem> v2History = getMemberSummaryV2UseCase.getSummaryByMemberId(member.getId())
            .challengerHistory();

        // Then
        assertThat(v1Records)
            .extracting(ChallengerInfoResponse::gisuId, ChallengerInfoResponse::challengerId)
            .containsExactly(tuple(gisu.getId(), challenger.getId()));
        assertThat(v2History)
            .extracting(ChallengerHistoryItem::gisuId, ChallengerHistoryItem::challengerId)
            .containsExactly(tuple(gisu.getId(), challenger.getId()));
    }

    private Member saveMember(String name, String email, Long schoolId) {
        return saveMemberPort.save(Member.create(name, name, email, schoolId, null));
    }

    private Long saveNotice(List<ChallengerPart> targetParts) {
        Notice notice = saveNoticePort.save(Notice.create("공지", "내용", learner.getId(), false, false));
        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(notice.getId())
            .targetGisuId(gisu.getId())
            .targetChallengerPart(targetParts)
            .targetNoticeTab(NoticeTab.CHALLENGER)
            .build());
        return notice.getId();
    }

    private boolean canReadNotice(Long noticeId) {
        return checkPermissionUseCase.check(member.getId(),
            ResourcePermission.of(ResourceType.NOTICE, noticeId, PermissionType.READ));
    }
}
