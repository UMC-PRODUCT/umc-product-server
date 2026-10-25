package com.umc.product.support.fixture;

import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.form.domain.Answer;
import com.umc.product.form.domain.Form;
import com.umc.product.form.domain.FormResponse;
import com.umc.product.form.domain.FormSection;
import com.umc.product.form.domain.Question;
import com.umc.product.form.domain.enums.QuestionType;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingChapterTransfer;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

import jakarta.persistence.EntityManager;

@Component
@Transactional
public class ProjectManualMatchingFixture {
    private final GisuFixture gisuFixture;
    private final ChapterFixture chapterFixture;
    private final SchoolFixture schoolFixture;
    private final ChallengerFixture challengerFixture;
    private final ChallengerRoleFixture roleFixture;
    private final SaveMemberPort saveMemberPort;
    private final SaveProjectPort saveProjectPort;
    private final LoadProjectPort loadProjectPort;
    private final SaveProjectPartQuotaPort saveQuotaPort;
    private final SaveProjectMemberPort saveProjectMemberPort;
    private final EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    private final EntityManager entityManager;

    public ProjectManualMatchingFixture(GisuFixture gisuFixture, ChapterFixture chapterFixture,
        SchoolFixture schoolFixture, ChallengerFixture challengerFixture, ChallengerRoleFixture roleFixture,
        SaveMemberPort saveMemberPort, SaveProjectPort saveProjectPort, LoadProjectPort loadProjectPort,
        SaveProjectPartQuotaPort saveQuotaPort, SaveProjectMemberPort saveProjectMemberPort,
        EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase, EntityManager entityManager) {
        this.gisuFixture = gisuFixture;
        this.chapterFixture = chapterFixture;
        this.schoolFixture = schoolFixture;
        this.challengerFixture = challengerFixture;
        this.roleFixture = roleFixture;
        this.saveMemberPort = saveMemberPort;
        this.saveProjectPort = saveProjectPort;
        this.loadProjectPort = loadProjectPort;
        this.saveQuotaPort = saveQuotaPort;
        this.saveProjectMemberPort = saveProjectMemberPort;
        this.evictAuthoritySnapshotCacheUseCase = evictAuthoritySnapshotCacheUseCase;
        this.entityManager = entityManager;
    }

    public Scenario 준비() {
        var gisu = gisuFixture.비활성_기수(95L);
        return 지부(gisu.getId(), chapterFixture.지부(gisu, "매칭 지부"));
    }

    public Scenario 다른_기수() {
        var gisu = gisuFixture.비활성_기수(96L);
        return 지부(gisu.getId(), chapterFixture.지부(gisu, "다른 기수 지부"));
    }

    public void 매칭지부_이동(Scenario from, Scenario to) {
        entityManager.persist(ProjectMatchingChapterTransfer.create(
            from.gisuId(), from.candidateId(), from.chapterId(), to.chapterId(), "지부 이동", from.staffId()));
    }

    public Scenario 다른_지부(Scenario scenario) {
        var gisu = entityManager.find(Gisu.class, scenario.gisuId());
        return 지부(gisu.getId(), chapterFixture.지부(gisu, "다른 매칭 지부"));
    }

    private Scenario 지부(Long gisuId, Chapter chapter) {
        var school = schoolFixture.지부에_소속된_학교(chapter.getName() + "학교", chapter);
        Long ownerId = 회원(gisuId, school.getId(), "PM", ChallengerPart.PLAN);
        Long staffId = 회원(gisuId, school.getId(), "지부장", ChallengerPart.PLAN);
        Long staffChallengerId = entityManager.createQuery("select c.id from Challenger c where c.memberId = :id", Long.class)
            .setParameter("id", staffId).getSingleResult();
        roleFixture.지부장(staffChallengerId, chapter.getId(), gisuId);
        Long candidateId = 회원(gisuId, school.getId(), "지원자", ChallengerPart.DESIGN);
        Long coPmId = 회원(gisuId, school.getId(), "보조PM", ChallengerPart.PLAN);
        Scenario context = new Scenario(gisuId, chapter.getId(), school.getId(), ownerId, staffId,
            candidateId, coPmId, null);
        return new Scenario(gisuId, chapter.getId(), school.getId(), ownerId, staffId, candidateId, coPmId,
            프로젝트(context, 1L));
    }

    public Long 회원(Scenario scenario, String name, ChallengerPart part) {
        return 회원(scenario.gisuId(), scenario.schoolId(), name, part);
    }

    private Long 회원(Long gisuId, Long schoolId, String name, ChallengerPart part) {
        Member member = saveMemberPort.save(Member.create(name, name, name + schoolId + "@test.com", schoolId, null));
        challengerFixture.챌린저(member.getId(), part, gisuId);
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(member.getId());
        return member.getId();
    }

    public Long 프로젝트(Scenario scenario, Long quota) {
        Project project = Project.createDraft(scenario.gisuId(), scenario.chapterId(), scenario.ownerId(),
            scenario.schoolId(), scenario.ownerId());
        project.updateBasicInfo("매칭 프로젝트", null, null, null, null);
        project.submit();
        project.publish();
        project = saveProjectPort.save(project);
        saveQuotaPort.save(ProjectPartQuota.create(project, ChallengerPart.DESIGN, quota, scenario.staffId()));
        return project.getId();
    }

    public void 총괄단(Scenario scenario) {
        Long challengerId = entityManager.createQuery("select c.id from Challenger c where c.memberId = :id", Long.class)
            .setParameter("id", scenario.staffId()).getSingleResult();
        roleFixture.중앙운영사무국_총괄(challengerId, scenario.gisuId());
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(scenario.staffId());
    }

    public ApplicationScenario 지원서_배정(Scenario scenario, boolean pending) {
        Project project = loadProjectPort.getById(scenario.projectId());
        Instant now = Instant.now();
        ProjectMatchingRound round = ProjectMatchingRound.create("디자인 1차", null, MatchingType.PLAN_DESIGN,
            MatchingPhase.FIRST, scenario.chapterId(), now.minusSeconds(10800), now.minusSeconds(7200),
            now.minusSeconds(3600), pending ? now.minusSeconds(600) : null);
        entityManager.persist(round);
        Form form = Form.createPublished(scenario.ownerId(), "지원서", false);
        entityManager.persist(form);
        FormSection section = FormSection.create(form, "공통", null, 1L);
        entityManager.persist(section);
        Question question = Question.create("소개", QuestionType.SHORT_TEXT, true, 1L);
        question.assignTo(section);
        entityManager.persist(question);
        FormResponse response = FormResponse.createDraft(form, scenario.candidateId());
        response.submit(now.minusSeconds(8000), null);
        entityManager.persist(response);
        Answer answer = Answer.create(response, question, QuestionType.SHORT_TEXT, "보존할 답변", Set.of(), Set.of());
        entityManager.persist(answer);
        ProjectApplicationForm applicationForm = ProjectApplicationForm.create(project, form.getId());
        entityManager.persist(applicationForm);
        ProjectApplication application = ProjectApplication.create(applicationForm, response.getId(),
            scenario.candidateId(), round);
        application.submit();
        application.applyAutoDecision(ProjectApplicationStatus.APPROVED, scenario.staffId());
        entityManager.persist(application);
        saveProjectMemberPort.save(ProjectMember.createFromApplication(application, ChallengerPart.DESIGN,
            scenario.staffId()));
        round.executeAutoDecision(scenario.staffId());
        entityManager.flush();
        return new ApplicationScenario(application.getId(), round.getId(), response.getId(), answer.getId());
    }

    public record Scenario(Long gisuId, Long chapterId, Long schoolId, Long ownerId, Long staffId,
                           Long candidateId, Long coPmId, Long projectId) {
    }

    public record ApplicationScenario(Long applicationId, Long roundId, Long responseId, Long answerId) {
    }
}
