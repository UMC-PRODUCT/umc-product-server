package com.umc.product.challenger.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import com.umc.product.challenger.application.port.in.command.RegisterChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;
import com.umc.product.challenger.application.port.out.SaveChallengerPointPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerPoint;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.adapter.out.persistence.MemberSystemRoleJpaRepository;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChallengerRoleFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("관리자 탈부 CSV 내보내기")
class ChallengerDeactivationExportIntegrationTest extends IntegrationTestSupport {

    private static final String PATH = "/api/v1/challenger/admin/deactivations/export";
    private static final String ADMIN = "deactivation-export-admin";
    private static final String SCHOOL_PRESIDENT = "deactivation-export-president";
    private static final ChallengerDeactivationReason VOLUNTARY =
        ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES;
    private static final ChallengerDeactivationReason NO_SHOW =
        ChallengerDeactivationReason.INVOLUNTARY_EVENT_NO_SHOW;
    private static final ChallengerDeactivationReason POINT_THRESHOLD =
        ChallengerDeactivationReason.INVOLUNTARY_POINT_THRESHOLD;

    @Autowired private GisuFixture gisuFixture;
    @Autowired private LoadGisuPort loadGisuPort;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SchoolFixture schoolFixture;
    @Autowired private ChallengerFixture challengerFixture;
    @Autowired private ChallengerRoleFixture challengerRoleFixture;
    @Autowired private SaveMemberPort saveMemberPort;
    @Autowired private SaveChallengerPort saveChallengerPort;
    @Autowired private SaveChallengerPointPort saveChallengerPointPort;
    @Autowired private MemberSystemRoleJpaRepository memberSystemRoleJpaRepository;
    @Autowired private RegisterChallengerDeactivationUseCase registerDeactivation;
    @Autowired private RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Gisu gisu;
    private Chapter ownChapter;
    private School ownSchool;
    private School otherSchool;
    private Long adminMemberId;

    @BeforeEach
    void setUp() {
        refreshMaintenanceStateUseCase.refresh();
        gisu = loadGisuPort.findActiveGisu().orElseGet(() -> gisuFixture.활성_기수(9011L));
        ownChapter = chapterFixture.지부(gisu, "CSV 본교 지부");
        Chapter otherChapter = chapterFixture.지부(gisu, "CSV 다른 지부");
        ownSchool = schoolFixture.지부에_소속된_학교("CSV 본교", ownChapter);
        otherSchool = schoolFixture.지부에_소속된_학교("CSV 타교", otherChapter);
        adminMemberId = createMember("exportAdmin", "관리자", "관리자", ownSchool).getId();
        memberSystemRoleJpaRepository.save(
            MemberSystemRole.create(adminMemberId, MemberSystemRoleType.SUPER_ADMIN)
        );
        authenticate(ADMIN, adminMemberId);
    }

    @Test
    @DisplayName("한국 날짜의 양 끝을 포함하며 실제 등록 기록만 최신순으로 내려받는다")
    void exportsOnlyRegisteredRowsInKoreanDateRange() throws Exception {
        Long first = register(createChallenger("first", ownSchool, ChallengerPart.PLAN), VOLUNTARY, null, "숨긴메모");
        Long newest = register(createChallenger("newest", ownSchool, ChallengerPart.DESIGN), NO_SHOW, null, null);
        Long before = register(createChallenger("before", ownSchool, ChallengerPart.PLAN), VOLUNTARY, null, null);
        Long after = register(createChallenger("after", ownSchool, ChallengerPart.PLAN), NO_SHOW, null, null);
        updateRegisteredAt(first, "2026-11-09T15:00:00Z");
        updateRegisteredAt(newest, "2026-11-10T14:59:59Z");
        updateRegisteredAt(before, "2026-11-09T14:59:59Z");
        updateRegisteredAt(after, "2026-11-10T15:00:00Z");
        Challenger pending = createChallenger("pending", ownSchool, ChallengerPart.PLAN);
        saveChallengerPointPort.save(ChallengerPoint.create(pending, PointType.CUSTOM, -20, null));
        Challenger legacy = createChallenger("legacy", ownSchool, ChallengerPart.DESIGN);
        legacy.changeStatus(ChallengerStatus.WITHDRAWN, adminMemberId, "보존 상태");
        saveChallengerPort.save(legacy);

        String csv = export(ADMIN, Map.of("startDate", "2026-11-10", "endDate", "2026-11-10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/csv;charset=UTF-8"))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"challenger-deactivations.csv\""))
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(csv).isEqualTo("\uFEFF기수,학교,파트,탈부 유형,탈부 사유,기타 사유,탈부 날짜,등록자 역할\r\n"
            + gisu.getGeneration() + ",CSV 본교,디자인,타의 탈부,행사 노쇼,,2026-11-10,\r\n"
            + gisu.getGeneration() + ",CSV 본교,기획,자진 탈부,개인 사정,,2026-11-10,\r\n");
    }

    @Test
    @DisplayName("선택 ID와 조건을 함께 적용하며 요청한 개인정보 열에 CSV 이스케이프를 적용한다")
    void exportsSelectedRowsAndEscapesOptInValues() throws Exception {
        Member member = createMember("formula", "=SUM(1,2)", "+닉네임", ownSchool);
        Challenger target = challengerFixture.챌린저(member.getId(), ChallengerPart.PLAN, gisu.getId());
        Long targetId = register(target, NO_SHOW, null, "메모,\"한 줄\"\n다음 줄");
        Challenger wrongTypeChallenger = createChallenger("point", ownSchool, ChallengerPart.PLAN);
        Long wrongType = register(wrongTypeChallenger, POINT_THRESHOLD, null, null);
        Long wrongPart = register(createChallenger("part", ownSchool, ChallengerPart.DESIGN), NO_SHOW, null, null);
        Long outside = register(createChallenger("outside", otherSchool, ChallengerPart.PLAN), NO_SHOW, null, null);
        register(createChallenger("not-selected", ownSchool, ChallengerPart.PLAN), NO_SHOW, null, null);

        String csv = export(ADMIN, Map.of(
            "gisuId", gisu.getId(),
            "chapterId", ownChapter.getId(),
            "schoolId", ownSchool.getId(),
            "part", ChallengerPart.PLAN,
            "reason", NO_SHOW,
            "types", List.of("VOLUNTARY", "EVENT_NO_SHOW"),
            "deactivationIds", List.of(targetId, wrongType, wrongPart, outside),
            "columns", List.of("NAME", "MEMO")
        )).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(csv).isEqualTo("\uFEFF메모,이름,닉네임\r\n"
            + "\"메모,\"\"한 줄\"\"\n다음 줄\",\"'=SUM(1,2)\",'+닉네임\r\n");
    }

    @Test
    @DisplayName("교내 회장단은 본교 등록 건만 내보내며 다른 학교 요청은 거부한다")
    void restrictsSchoolPresidentToOwnSchool() throws Exception {
        Challenger president = createChallenger("president", ownSchool, ChallengerPart.PLAN);
        challengerRoleFixture.학교_회장(president.getId(), ownSchool.getId(), gisu.getId());
        authenticate(SCHOOL_PRESIDENT, president.getMemberId());
        Long own = register(createChallenger("own", ownSchool, ChallengerPart.PLAN), VOLUNTARY, null, null);
        Long outside = register(createChallenger("other", otherSchool, ChallengerPart.PLAN), NO_SHOW, null, null);

        String csv = export(SCHOOL_PRESIDENT, Map.of(
            "deactivationIds", List.of(own, outside),
            "columns", List.of("NAME")
        )).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).isEqualTo("\uFEFF이름,닉네임\r\nown,own\r\n");
        export(SCHOOL_PRESIDENT, Map.of("schoolId", otherSchool.getId(), "deactivationIds", List.of(outside)))
            .andExpect(status().isForbidden());
        Gisu pastGisu = gisuFixture.비활성_기수(9010L);
        export(SCHOOL_PRESIDENT, Map.of("gisuId", pastGisu.getId(), "deactivationIds", List.of(own)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("전체 내보내기는 유효한 기간이 필요하고 선택은 1,000건까지 허용한다")
    void rejectsInvalidDateRangeAndExcessiveSelection() throws Exception {
        export(ADMIN, Map.of()).andExpect(status().isBadRequest());
        export(ADMIN, Map.of("startDate", "2026-11-03", "endDate", "2026-11-01"))
            .andExpect(status().isBadRequest());
        export(ADMIN, Map.of("deactivationIds", LongStream.rangeClosed(1, 1_001).boxed().toList()))
            .andExpect(status().isBadRequest());
    }

    private ResultActions export(String token, Map<String, ?> body) throws Exception {
        return mockMvc.perform(post(PATH)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(body)));
    }

    private void authenticate(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }

    private Member createMember(String emailLocalPart, String name, String nickname, School school) {
        Member member = Member.create(name, nickname, emailLocalPart + "@example.com", school.getId(), null);
        return saveMemberPort.save(member);
    }

    private Challenger createChallenger(String name, School school, ChallengerPart part) {
        return challengerFixture.챌린저(createMember(name, name, name, school).getId(), part, gisu.getId());
    }

    private Long register(Challenger target, ChallengerDeactivationReason reason, String reasonDetail, String memo) {
        return registerDeactivation.register(RegisterChallengerDeactivationCommand.of(
            target.getId(), reason, reasonDetail, memo, List.of(), List.of(), adminMemberId
        ));
    }

    private void updateRegisteredAt(Long id, String time) {
        jdbcTemplate.update(
            "UPDATE challenger_deactivation SET created_at = ? WHERE id = ?", OffsetDateTime.parse(time), id
        );
    }
}
