package com.umc.product.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookMissionPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notification.application.port.in.SendWebhookAlarmUseCase;
import com.umc.product.organization.application.port.out.command.SaveGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.SchoolFixture;

@TestPropertySource(properties = "app.seed.enabled=true")
class TrackSeedIntegrationTest extends IntegrationTestSupport {

    @Autowired private SaveGisuPort saveGisuPort;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SchoolFixture schoolFixture;
    @Autowired private SaveMemberPort saveMemberPort;
    @Autowired private GetMemberUseCase getMemberUseCase;
    @Autowired private GetChallengerUseCase getChallengerUseCase;
    @Autowired private GetCurriculumUseCase getCurriculumUseCase;
    @Autowired private LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    @Autowired private LoadOriginalWorkbookMissionPort loadOriginalWorkbookMissionPort;

    @MockitoBean private SendWebhookAlarmUseCase sendWebhookAlarmUseCase;

    private Long gisuId;
    private Long chapterId;
    private Long schoolId;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();
        var gisu = saveGisuPort.save(Gisu.create(
            99L, now.minus(1, ChronoUnit.DAYS), now.plus(90, ChronoUnit.DAYS), false));
        var chapter = chapterFixture.지부(gisu, "시딩 지부");
        var school = schoolFixture.지부에_소속된_학교("시딩 학교", chapter);
        gisuId = gisu.getId();
        chapterId = chapter.getId();
        schoolId = school.getId();
    }

    @Test
    @DisplayName("단건 시딩으로 챌린저의 단일 파트와 인프라 여부를 저장한다")
    void seedChallengerWithPartAndInfra() throws Exception {
        var member = saveMemberPort.save(Member.create(
            "학습자", "학습자", "part-seed@example.com", schoolId, null));
        String request = """
            {"memberId":%d,"gisuId":%d,"part":"WEB_PRODUCT_ENGINEER","infra":true}
            """.formatted(member.getId(), gisuId);

        mockMvc.perform(post("/test/seed/challenger")
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        var challenger = getChallengerUseCase.getByMemberIdAndGisuId(member.getId(), gisuId);
        assertThat(challenger.part()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(challenger.infra()).isTrue();
    }

    @Test
    @DisplayName("파트 분포 시딩으로 학교별 회원과 해당 파트 챌린저를 함께 생성한다")
    void seedChallengersByPartAndSchool() throws Exception {
        long memberCount = getMemberUseCase.countAll();
        String request = """
            {"gisuId":%d,"countPerPartPerSchool":1,
             "parts":["WEB_PRODUCT_ENGINEER"],"chapterIds":[%d]}
            """.formatted(gisuId, chapterId);

        mockMvc.perform(post("/test/seed/challengers")
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.totalCreated").value(1))
            .andExpect(jsonPath("$.result.totalFailed").value(0))
            .andExpect(jsonPath("$.result.perCellSummary[0].part").value("WEB_PRODUCT_ENGINEER"));

        assertThat(getMemberUseCase.countAll()).isEqualTo(memberCount + 1);
        assertThat(getChallengerUseCase.getAllByGisuId(gisuId)).singleElement().satisfies(challenger -> {
            assertThat(challenger.part()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
            assertThat(challenger.infra()).isFalse();
            assertThat(getMemberUseCase.listIdsBySchoolId(schoolId)).contains(challenger.memberId());
        });
    }

    @Test
    @DisplayName("파트 커리큘럼 시딩으로 주차별 원본 워크북과 미션까지 저장한다")
    void seedPartCurriculumWithWorkbooksAndMissions() throws Exception {
        String request = """
            {"gisuId":%d,"parts":["INFRA"],
             "weeksPerCurriculum":2,"missionsPerWorkbook":1}
            """.formatted(gisuId);

        var response = mockMvc.perform(post("/test/seed/curriculum")
                .contentType(MediaType.APPLICATION_JSON).content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.createdCurriculumIds.length()").value(1))
            .andExpect(jsonPath("$.result.createdWeeklyCurriculumIds.length()").value(2))
            .andExpect(jsonPath("$.result.createdOriginalWorkbookIds.length()").value(2))
            .andExpect(jsonPath("$.result.createdMissionIds.length()").value(2))
            .andExpect(jsonPath("$.result.curriculumFailed").value(0))
            .andReturn().getResponse();

        JsonNode result = objectMapper.readTree(response.getContentAsString()).path("result");
        var curriculum = getCurriculumUseCase.getCurriculumOverview(gisuId, ChallengerPart.INFRA, null);
        assertThat(curriculum.curriculumId()).isEqualTo(result.path("createdCurriculumIds").get(0).asLong());
        assertThat(curriculum.part()).isEqualTo(ChallengerPart.INFRA);
        for (JsonNode workbookId : result.path("createdOriginalWorkbookIds")) {
            var workbook = loadOriginalWorkbookPort.getById(workbookId.asLong());
            assertThat(workbook.getOriginalWorkbookStatus()).isEqualTo(OriginalWorkbookStatus.READY);
            assertThat(loadOriginalWorkbookMissionPort.findByOriginalWorkbookId(workbook.getId())).hasSize(1);
        }
    }
}
