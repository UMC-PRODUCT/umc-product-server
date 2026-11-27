package com.umc.product.notice.adapter.in.web;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.adapter.out.persistence.MemberSystemRoleJpaRepository;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.organization.application.port.out.command.SaveChapterSchoolPort;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChallengerRoleFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("공지 수신 현황과 재알림 API")
class NoticeReadStatusReminderIntegrationTest extends IntegrationTestSupport {

    @Autowired private GisuFixture gisuFixture;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SchoolFixture schoolFixture;
    @Autowired private SaveChapterSchoolPort saveChapterSchoolPort;
    @Autowired private ChallengerFixture challengerFixture;
    @Autowired private ChallengerRoleFixture roleFixture;
    @Autowired private SaveChallengerPort saveChallengerPort;
    @Autowired private SaveMemberPort saveMemberPort;
    @Autowired private MemberSystemRoleJpaRepository systemRoles;
    @Autowired private SaveNoticePort saveNoticePort;
    @Autowired private SaveNoticeTargetPort saveTargetPort;
    @Autowired private EvictAuthoritySnapshotCacheUseCase authorityCache;
    @Autowired private RefreshMaintenanceStateUseCase maintenance;
    @Autowired private JdbcTemplate jdbc;

    private final List<Long> memberIds = new ArrayList<>();
    private Gisu gisu;
    private School school;
    private Long schoolId;
    private Challenger author;

    @BeforeEach
    void setUp() {
        maintenance.refresh();
        gisu = gisuFixture.비활성_기수(9701L);
        school = schoolFixture.지부에_소속된_학교(chapterFixture.지부(gisu));
        schoolId = school.getId();
        author = challenger("author", schoolId, gisu, ChallengerPart.DESIGN);
        roleFixture.학교_회장(author.getId(), schoolId, gisu.getId());
    }

    @AfterEach
    void clearCache() {
        authorityCache.evictByMemberIds(memberIds);
    }

    @Test
    void 학교_운영진_공지는_다른_파트_교내_운영진도_집계하지만_재알림을_보낼_수_없다() throws Exception {
        Challenger staff = challenger("staff", schoolId, gisu, ChallengerPart.PLAN);
        roleFixture.학교_파트장(staff.getId(), ChallengerPart.PLAN, schoolId, gisu.getId());
        Challenger ordinary = challenger("ordinary", schoolId, gisu, ChallengerPart.DESIGN);
        Long noticeId = notice(NoticeTab.SCHOOL_PART_LEADER);

        read(noticeId, "ordinary").andExpect(status().isForbidden());
        read(noticeId, "staff").andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notices/{id}/read-statics", noticeId).header("Authorization", "Bearer author"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalCount").value(2))
            .andExpect(jsonPath("$.result.readCount").value(1))
            .andExpect(jsonPath("$.result.unreadCount").value(1));
        list(noticeId, "READ").andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.content[0].challengerId").value(staff.getId()));
        list(noticeId, "UNREAD").andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.content[0].challengerId").value(author.getId()));
        remind(noticeId, "author", author.getId(), ordinary.getId()).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOTICE-0005"));
        remindAll(noticeId, "author").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOTICE-0005"));
        assertThat(queuedRecipientIds()).isEmpty();
    }

    @Test
    void 챌린저_공지의_요청자와_수신자와_읽음_상태를_검사하고_적격_회원만_발송_예약한다() throws Exception {
        Challenger read = challenger("read", schoolId, gisu, ChallengerPart.DESIGN);
        Challenger staff = challenger("staff", schoolId, gisu, ChallengerPart.DESIGN);
        roleFixture.학교_부회장(staff.getId(), schoolId, gisu.getId());
        Long noticeId = notice(NoticeTab.CHALLENGER);

        Challenger unread = challenger("unread", schoolId, gisu, ChallengerPart.DESIGN);
        staff.changePart(ChallengerPart.PLAN);
        saveChallengerPort.save(staff);
        read(noticeId, "read").andExpect(status().isOk());

        remind(noticeId, "staff", unread.getId()).andExpect(status().isForbidden());
        remindAll(noticeId, "staff").andExpect(status().isForbidden());
        remind(noticeId, "author", read.getId(), staff.getId()).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOTICE-0015"));
        assertThat(queuedRecipientIds()).isEmpty();
        remind(noticeId, "author", read.getId(), unread.getId(), unread.getId(), staff.getId())
            .andExpect(status().isOk());
        assertThat(queuedRecipientIds()).containsExactly(unread.getMemberId());
        remindAll(noticeId, "author").andExpect(status().isOk());
        assertThat(queuedRecipientIds()).containsExactlyInAnyOrder(
            unread.getMemberId(), unread.getMemberId(), author.getMemberId());
    }

    @Test
    void 대상_기수에서_탈부하거나_제명된_회원은_수신_현황과_재알림에서_제외한다() throws Exception {
        Gisu other = gisuWithSchool(9700L);
        Challenger authorHistory = challengerFixture.웹(author.getMemberId(), other.getId());
        authorHistory.changeStatus(ChallengerStatus.WITHDRAWN, author.getMemberId(), "이전 기수 탈부");
        saveChallengerPort.save(authorHistory);

        Challenger withdrawn = challenger("withdrawn", schoolId, gisu, ChallengerPart.DESIGN);
        roleFixture.학교_부회장(withdrawn.getId(), schoolId, gisu.getId());
        challengerFixture.웹(withdrawn.getMemberId(), other.getId());
        withdrawn.changeStatus(ChallengerStatus.WITHDRAWN, author.getMemberId(), "탈부");
        saveChallengerPort.save(withdrawn);
        Challenger expelled = challenger("expelled", schoolId, gisu, ChallengerPart.DESIGN);
        expelled.changeStatus(ChallengerStatus.EXPELLED, author.getMemberId(), "제명");
        saveChallengerPort.save(expelled);
        Long noticeId = notice(NoticeTab.CHALLENGER);

        mockMvc.perform(get("/api/v1/notices/{id}/read-statics", noticeId).header("Authorization", "Bearer author"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalCount").value(1))
            .andExpect(jsonPath("$.result.unreadCount").value(1));
        list(noticeId, "UNREAD").andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.content[0].challengerId").value(author.getId()));
        remind(noticeId, "author", withdrawn.getId(), expelled.getId()).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOTICE-0015"));
        remindAll(noticeId, "author").andExpect(status().isOk());
        assertThat(queuedRecipientIds()).containsExactly(author.getMemberId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 대상_기수_이력이_없으면_가장_나중에_생성한_이력으로_읽음과_재알림_대상을_정한다(boolean allGisus) throws Exception {
        Gisu higher = gisuWithSchool(9700L);
        Gisu lower = gisuWithSchool(9699L);
        Challenger president = challenger("president", schoolId, higher, ChallengerPart.DESIGN);
        Challenger vice = challenger("vice", schoolId, higher, ChallengerPart.DESIGN);
        roleFixture.중앙운영사무국_총괄(president.getId(), higher.getId());
        roleFixture.중앙운영사무국_부총괄(vice.getId(), higher.getId());

        Challenger expelled = challengerFixture.웹(president.getMemberId(), lower.getId());
        expelled.changeStatus(ChallengerStatus.EXPELLED, author.getMemberId(), "제명");
        saveChallengerPort.save(expelled);
        Challenger graduated = challengerFixture.웹(vice.getMemberId(), lower.getId());
        graduated.changeStatus(ChallengerStatus.GRADUATED, author.getMemberId(), "수료");
        saveChallengerPort.save(graduated);
        Long noticeId = notice(NoticeTab.CHALLENGER, allGisus ? null : gisu.getId());

        remindAll(noticeId, "author").andExpect(status().isOk());
        // 다른 기수의 총괄단은 공지를 열람할 수 있지만 해당 기수만 지정한 재알림의 게시 대상에서는 빠진다.
        List<Long> recipients = allGisus
            ? List.of(author.getMemberId(), vice.getMemberId()) : List.of(author.getMemberId());
        assertThat(queuedRecipientIds()).containsExactlyInAnyOrderElementsOf(recipients);
        read(noticeId, "president").andExpect(status().isBadRequest());
        read(noticeId, "vice").andExpect(status().isOk());
        list(noticeId, "READ").andExpect(jsonPath("$.result.content.length()").value(1));
        list(noticeId, "UNREAD").andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.content[0].challengerId").value(author.getId()));
    }

    @Test
    void 챌린저_이력이_없는_SUPER_ADMIN은_발송할_수_있고_수신_통계에는_포함되지_않는다() throws Exception {
        Long admin = member("admin", schoolId);
        systemRoles.save(MemberSystemRole.create(admin, MemberSystemRoleType.SUPER_ADMIN));
        Long noticeId = notice(NoticeTab.CHALLENGER);

        mockMvc.perform(get("/api/v1/notices/{id}/read-statics", noticeId).header("Authorization", "Bearer admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalCount").value(1));
        remindAll(noticeId, "admin").andExpect(status().isOk());
        remindAll(noticeId, "author").andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value("NOTICE-0014"));
        assertThat(queuedRecipientIds()).containsExactly(author.getMemberId());
    }

    @Test
    void 개별_발송은_전체_발송_전후로_가능하고_전체_발송은_조회한_페이지와_관계없이_하루_한_번이다() throws Exception {
        List<Long> recipients = new ArrayList<>();
        recipients.add(author.getMemberId());
        for (int i = 0; i < 20; i++) {
            recipients.add(challenger("unread" + i, schoolId, gisu, ChallengerPart.DESIGN).getMemberId());
        }
        Long noticeId = notice(NoticeTab.CHALLENGER);

        list(noticeId, "UNREAD").andExpect(jsonPath("$.result.hasNext").value(true));
        remind(noticeId, "author", author.getId()).andExpect(status().isOk());
        remindAll(noticeId, "author").andExpect(status().isOk());
        remindAll(noticeId, "author").andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value("NOTICE-0014"));
        remind(noticeId, "author", author.getId()).andExpect(status().isOk());

        List<Long> expected = new ArrayList<>(recipients);
        expected.addAll(List.of(author.getMemberId(), author.getMemberId()));
        assertThat(queuedRecipientIds()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 전체_재알림에_미확인_대상자가_없으면_발송일을_기록하지_않는다() throws Exception {
        Long noticeId = notice(NoticeTab.CHALLENGER);
        read(noticeId, "author").andExpect(status().isOk());

        remindAll(noticeId, "author").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOTICE-0015"));
        assertThat(queuedRecipientIds()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT last_reminder_date IS NULL FROM notice WHERE id = ?",
            Boolean.class, noticeId)).isTrue();
    }

    @Test
    void 타_기수_총괄의_읽음을_집계하고_소속_표시값이_없는_행도_기존_앱에서_읽을_수_있다() throws Exception {
        Gisu other = gisuFixture.비활성_기수(9700L);
        Long otherSchool = schoolFixture.학교().getId();
        Challenger central = challenger("central", otherSchool, other, null);
        roleFixture.중앙운영사무국_총괄(central.getId(), other.getId());
        Long noticeId = notice(NoticeTab.SCHOOL_CORE);

        read(noticeId, "central").andExpect(status().isOk());
        read(noticeId, "central").andExpect(status().isOk());
        list(noticeId, "READ").andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.content[0].challengerId").value(central.getId()))
            .andExpect(jsonPath("$.result.content[0].part").value(""))
            .andExpect(jsonPath("$.result.content[0].chapterName").value(""));
    }

    @Test
    void 같은_회원의_동시_읽음_요청은_모두_성공하고_한_번만_기록한다() throws Exception {
        Long noticeId = notice(NoticeTab.CHALLENGER);
        CyclicBarrier start = new CyclicBarrier(2);
        Callable<Integer> recordRead = () -> {
            start.await(10, SECONDS);
            return read(noticeId, "author").andReturn().getResponse().getStatus();
        };

        try (ExecutorService workers = Executors.newFixedThreadPool(2)) {
            Future<Integer> first = workers.submit(recordRead);
            Future<Integer> second = workers.submit(recordRead);
            assertThat(List.of(first.get(10, SECONDS), second.get(10, SECONDS))).containsExactly(200, 200);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM notice_read WHERE notice_id = ?", Long.class, noticeId))
            .isEqualTo(1L);
    }

    private Gisu gisuWithSchool(long generation) {
        Gisu other = gisuFixture.비활성_기수(generation);
        saveChapterSchoolPort.save(ChapterSchool.create(chapterFixture.지부(other), school));
        return other;
    }

    private Challenger challenger(String name, Long school, Gisu generation, ChallengerPart part) {
        Long id = member(name, school);
        return part == null ? challengerFixture.수강_없는_소속(id, generation.getId())
            : challengerFixture.챌린저(id, part, generation.getId());
    }

    private Long member(String name, Long school) {
        Long id = saveMemberPort.save(Member.create(name, name, name + "@test.com", school, null)).getId();
        memberIds.add(id);
        authorityCache.evictByMemberIds(List.of(id));
        given(jwtTokenProvider.validateAccessToken(name)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(name)).willReturn(id);
        given(jwtTokenProvider.getRolesFromAccessToken(name)).willReturn(List.of());
        return id;
    }

    private Long notice(NoticeTab tab) {
        return notice(tab, gisu.getId());
    }

    private Long notice(NoticeTab tab, Long targetGisuId) {
        Long id = saveNoticePort.save(Notice.create("공지", "본문", author.getMemberId(), false, false)).getId();
        saveTargetPort.save(NoticeTarget.builder().noticeId(id).targetGisuId(targetGisuId).targetSchoolId(schoolId)
            .targetChallengerPart(tab == NoticeTab.SCHOOL_CORE ? List.of() : List.of(ChallengerPart.DESIGN))
            .targetNoticeTab(tab).build());
        return id;
    }

    private ResultActions read(Long id, String token) throws Exception {
        return mockMvc.perform(post("/api/v1/notices/{id}/read", id).header("Authorization", "Bearer " + token));
    }

    private ResultActions list(Long id, String statusFilter) throws Exception {
        return mockMvc.perform(get("/api/v1/notices/{id}/read-status", id).header("Authorization", "Bearer author")
            .param("filterType", "ALL").param("status", statusFilter)).andExpect(status().isOk());
    }

    private ResultActions remind(Long id, String token, Long... targetIds) throws Exception {
        return mockMvc.perform(post("/api/v1/notices/{id}/reminders", id)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("targetIds", List.of(targetIds)))));
    }

    private ResultActions remindAll(Long id, String token) throws Exception {
        return mockMvc.perform(post("/api/v1/notices/{id}/reminders/all", id)
            .header("Authorization", "Bearer " + token));
    }

    private List<Long> queuedRecipientIds() {
        return jdbc.queryForList("""
            SELECT jsonb_array_elements_text(payload -> 'memberIds')::bigint
            FROM event_outbox WHERE event_type = 'notification.fcm.requested'
            """, Long.class);
    }
}
