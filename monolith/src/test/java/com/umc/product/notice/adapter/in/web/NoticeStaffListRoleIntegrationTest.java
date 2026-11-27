package com.umc.product.notice.adapter.in.web;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChallengerRoleFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("겸임 계정의 선택 직책별 공지 목록·검색")
class NoticeStaffListRoleIntegrationTest extends IntegrationTestSupport {

    @Autowired private GisuFixture gisuFixture;
    @Autowired private LoadGisuPort loadGisuPort;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SchoolFixture schoolFixture;
    @Autowired private ChallengerFixture challengerFixture;
    @Autowired private ChallengerRoleFixture roles;
    @Autowired private MemberFixture memberFixture;
    @Autowired private SaveMemberPort members;
    @Autowired private SaveNoticePort notices;
    @Autowired private SaveNoticeTargetPort targets;
    @Autowired private RefreshMaintenanceStateUseCase maintenance;

    private Long gisuId;
    private Long chapterId;
    private Long schoolId;
    private Long otherSchoolId;
    private Long memberId;
    private Long challengerId;
    private Long schoolRoleId;

    @BeforeEach
    void setUp() {
        maintenance.refresh();
        var gisu = loadGisuPort.findActiveGisu().orElseGet(() -> gisuFixture.활성_기수(9711L));
        gisuId = gisu.getId();
        Chapter chapter = chapterFixture.지부(gisu);
        chapterId = chapter.getId();
        schoolId = schoolFixture.지부에_소속된_학교("프로필 학교", chapter).getId();
        otherSchoolId = schoolFixture.지부에_소속된_학교("같은 지부의 다른 학교", chapter).getId();
        memberId = members.save(Member.create("운영진", "운영진", "viewer@example.com", schoolId, null)).getId();
        challengerId = challengerFixture.웹(memberId, gisuId).getId();
        schoolRoleId = roles.학교_회장(challengerId, schoolId, gisuId).getId();
        given(jwtTokenProvider.validateAccessToken("staff-token")).willReturn(true);
        given(jwtTokenProvider.parseAccessToken("staff-token")).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken("staff-token")).willReturn(List.of());

        notice("전국 회장단", null, NoticeTab.SCHOOL_CORE);
        notice("전국 운영진", null, NoticeTab.SCHOOL_PART_LEADER);
        notice("본교 회장단", schoolId, NoticeTab.SCHOOL_CORE);
        notice("본교 운영진", schoolId, NoticeTab.SCHOOL_PART_LEADER);
        notice("타교 회장단", otherSchoolId, NoticeTab.SCHOOL_CORE);
        notice("타교 운영진", otherSchoolId, NoticeTab.SCHOOL_PART_LEADER);
        notice("중앙 운영진", null, NoticeTab.CENTRAL_MEMBER);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/notices", "/api/v1/notices/search"})
    void 지부장과_학교_회장을_겸임하면_선택한_직책의_운영진_공지만_조회한다(String path) throws Exception {
        // given
        Long chapterRoleId = roles.지부장(challengerId, chapterId, gisuId).getId();

        // when & then
        expectTitles(request(path).param("challengerRoleId", chapterRoleId.toString()), "전국 회장단", "전국 운영진");
        expectTitles(request(path).param("challengerRoleId", schoolRoleId.toString()),
            "전국 회장단", "전국 운영진", "본교 회장단", "본교 운영진");
        mockMvc.perform(request(path).param("challengerRoleId", schoolRoleId.toString()).param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.totalElements").value(4));
        expectTitles(request(path), "전국 회장단", "전국 운영진");
        mockMvc.perform(request(path).param("schoolId", schoolId.toString()))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/notices", "/api/v1/notices/search"})
    void 중앙_운영국과_학교_회장을_겸임하면_학교_View에서_중앙과_타교_공지를_제외한다(String path) throws Exception {
        // given
        Long centralRoleId = roles.중앙운영사무국_일반_운영진(challengerId, gisuId).getId();
        String[] allStaffNotices = {"전국 회장단", "전국 운영진", "본교 회장단", "본교 운영진", "타교 회장단", "타교 운영진", "중앙 운영진"};

        // when & then
        expectTitles(request(path).param("challengerRoleId", centralRoleId.toString()), allStaffNotices);
        expectTitles(request(path).param("challengerRoleId", schoolRoleId.toString()),
            "전국 회장단", "전국 운영진", "본교 회장단", "본교 운영진");
        expectTitles(request(path), allStaffNotices);
        mockMvc.perform(request(path).param("challengerRoleId", schoolRoleId.toString())
                .param("schoolId", otherSchoolId.toString()))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/notices", "/api/v1/notices/search"})
    void 챌린저_공지에서도_학교_회장_직책을_선택하면_타교_공지가_빠진다(String path) throws Exception {
        // given
        Long centralRoleId = roles.중앙운영사무국_일반_운영진(challengerId, gisuId).getId();
        notice("본교 챌린저", schoolId, NoticeTab.CHALLENGER);
        notice("타교 챌린저", otherSchoolId, NoticeTab.CHALLENGER);

        // when & then
        expectTitles(request(path, NoticeTab.CHALLENGER).param("challengerRoleId", centralRoleId.toString()),
            "본교 챌린저", "타교 챌린저");
        expectTitles(request(path, NoticeTab.CHALLENGER).param("challengerRoleId", schoolRoleId.toString()),
            "본교 챌린저");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/notices", "/api/v1/notices/search"})
    void 타인의_직책이나_이전_기수의_직책을_선택하면_거부한다(String path) throws Exception {
        // given
        Long anotherMemberId = members.save(Member.create("타교", "타교", "other@example.com", otherSchoolId, null))
            .getId();
        Long anotherChallengerId = challengerFixture.웹(anotherMemberId, gisuId).getId();
        Long foreignRoleId = roles.학교_회장(anotherChallengerId, otherSchoolId, gisuId).getId();
        Long oldGisuId = gisuFixture.비활성_기수(9710L).getId();
        Long oldChallengerId = challengerFixture.웹(memberId, oldGisuId).getId();
        Long expiredRoleId = roles.중앙운영사무국_총괄(oldChallengerId, oldGisuId).getId();

        // when & then
        mockMvc.perform(request(path).param("challengerRoleId", foreignRoleId.toString()))
            .andExpect(status().isForbidden());
        mockMvc.perform(request(path).param("challengerRoleId", expiredRoleId.toString()))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerRoleType.class, names = {"CENTRAL_PRESIDENT", "CENTRAL_VICE_PRESIDENT"})
    void 총괄단_View는_과거_기수의_챌린저_공지만_조회할_수_있다(ChallengerRoleType roleType) throws Exception {
        Long roleId = roleType == ChallengerRoleType.CENTRAL_PRESIDENT
            ? roles.중앙운영사무국_총괄(challengerId, gisuId).getId()
            : roles.중앙운영사무국_부총괄(challengerId, gisuId).getId();
        Long oldGisuId = gisuFixture.비활성_기수(9710L).getId();
        notice("이전 기수 챌린저", null, NoticeTab.CHALLENGER, oldGisuId);

        for (String path : List.of("/api/v1/notices", "/api/v1/notices/search")) {
            mockMvc.perform(request(path, NoticeTab.CENTRAL_MEMBER, oldGisuId)
                    .param("challengerRoleId", roleId.toString()))
                .andExpect(status().isForbidden());
            expectTitles(request(path, NoticeTab.CHALLENGER, oldGisuId).param("challengerRoleId", roleId.toString()),
                "이전 기수 챌린저");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/notices", "/api/v1/notices/search"})
    void 직책_ID를_생략하면_조회_기수에서_맡았던_직책으로_운영진_공지를_조회한다(String path) throws Exception {
        Long oldGisuId = gisuFixture.비활성_기수(9710L).getId();
        notice("이전 기수 중앙 공지", null, NoticeTab.CENTRAL_MEMBER, oldGisuId);
        mockMvc.perform(request(path, NoticeTab.CENTRAL_MEMBER, oldGisuId))
            .andExpect(status().isForbidden());

        Long oldChallengerId = challengerFixture.웹(memberId, oldGisuId).getId();
        roles.중앙운영사무국_부총괄(oldChallengerId, oldGisuId);
        expectTitles(request(path, NoticeTab.CENTRAL_MEMBER, oldGisuId), "이전 기수 중앙 공지");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/notices", "/api/v1/notices/search"})
    void 슈퍼_관리자는_직책_ID_없이_과거_기수_운영진_공지를_조회한다(String path) throws Exception {
        memberFixture.시스템_역할_부여(memberId, MemberSystemRoleType.SUPER_ADMIN);
        Long oldGisuId = gisuFixture.비활성_기수(9710L).getId();
        notice("이전 기수 운영진", null, NoticeTab.CENTRAL_MEMBER, oldGisuId);

        expectTitles(request(path, NoticeTab.CENTRAL_MEMBER, oldGisuId), "이전 기수 운영진");
    }

    private void expectTitles(MockHttpServletRequestBuilder request, String... titles) throws Exception {
        mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalElements").value(titles.length))
            .andExpect(jsonPath("$.result.content[*].title", containsInAnyOrder(titles)));
    }

    private MockHttpServletRequestBuilder request(String path) {
        return request(path, NoticeTab.SCHOOL_CORE);
    }

    private MockHttpServletRequestBuilder request(String path, NoticeTab tab) {
        return request(path, tab, gisuId);
    }

    private MockHttpServletRequestBuilder request(String path, NoticeTab tab, Long targetGisuId) {
        MockHttpServletRequestBuilder request = get(path).header("Authorization", "Bearer staff-token")
            .param("gisuId", targetGisuId.toString()).param("noticeTab", tab.name());
        return path.endsWith("/search") ? request.param("keyword", "회의") : request;
    }

    private void notice(String title, Long targetSchoolId, NoticeTab tab) {
        notice(title, targetSchoolId, tab, gisuId);
    }

    private void notice(String title, Long targetSchoolId, NoticeTab tab, Long targetGisuId) {
        Long id = notices.save(Notice.create(title, "회의 일정", memberId, false, false)).getId();
        targets.save(NoticeTarget.builder().noticeId(id).targetGisuId(targetGisuId).targetSchoolId(targetSchoolId)
            .targetChallengerPart(List.of()).targetNoticeTab(tab).build());
    }
}
