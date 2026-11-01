package com.umc.product.support.fixture;

import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.form.domain.Answer;
import com.umc.product.form.domain.Form;
import com.umc.product.form.domain.FormResponse;
import com.umc.product.form.domain.FormSection;
import com.umc.product.form.domain.Question;
import com.umc.product.form.domain.enums.QuestionType;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.domain.School;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;

import jakarta.persistence.EntityManager;

@Component
@Transactional
public class ProjectDeletionFixture {

    private final EntityManager entityManager;
    private final GisuFixture gisuFixture;
    private final ChapterFixture chapterFixture;

    public ProjectDeletionFixture(EntityManager entityManager, GisuFixture gisuFixture, ChapterFixture chapterFixture) {
        this.entityManager = entityManager;
        this.gisuFixture = gisuFixture;
        this.chapterFixture = chapterFixture;
    }

    public Long 회원(String name) {
        School school = School.create(name + "학교", null, null);
        entityManager.persist(school);
        Member member = Member.create(name, name, name + "@test.com", school.getId(), null);
        entityManager.persist(member);
        return member.getId();
    }

    public Scenario 삭제대상(Long ownerId, Long applicantId, boolean pendingPublication, boolean otherAssignment) {
        var gisu = gisuFixture.비활성_기수(99L);
        var chapter = chapterFixture.지부(gisu, "삭제 테스트 지부");
        Project project = 등록완료(gisu.getId(), chapter.getId(), ownerId);
        Instant now = Instant.now();
        ProjectMatchingRound round = ProjectMatchingRound.create("개발 1차", null,
            MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, chapter.getId(),
            now.minusSeconds(10800), now.minusSeconds(7200), now.minusSeconds(3600),
            pendingPublication ? now.minusSeconds(1800) : null);
        entityManager.persist(round);
        Form form = Form.createPublished(ownerId, "프로젝트 지원서", false);
        entityManager.persist(form);
        FormSection section = FormSection.create(form, "공통", null, 1L);
        entityManager.persist(section);
        Question question = Question.create("자기소개", QuestionType.SHORT_TEXT, true, 1L);
        question.assignTo(section);
        entityManager.persist(question);
        FormResponse response = FormResponse.createDraft(form, applicantId);
        response.submit(now.minusSeconds(8000), null);
        entityManager.persist(response);
        Answer answer = Answer.create(response, question, QuestionType.SHORT_TEXT, "답변", Set.of(), Set.of());
        entityManager.persist(answer);
        ProjectApplicationForm applicationForm = ProjectApplicationForm.create(project, form.getId());
        entityManager.persist(applicationForm);
        entityManager.persist(ProjectApplicationFormPolicy.createCommon(applicationForm, section.getId()));
        ProjectApplication application = ProjectApplication.create(applicationForm, response.getId(), applicantId, round);
        application.submit();
        application.applyAutoDecision(ProjectApplicationStatus.APPROVED, ownerId);
        entityManager.persist(application);
        entityManager.persist(ProjectMember.createFromApplication(application, ChallengerPart.WEB, ownerId));
        entityManager.persist(ProjectPartQuota.create(project, ChallengerPart.WEB, 2L, ownerId));
        Long otherProjectId = null;
        if (otherAssignment) {
            Project other = 등록완료(gisu.getId(), chapter.getId(), ownerId);
            entityManager.persist(ProjectMember.create(other, applicantId, ChallengerPart.WEB, ownerId));
            otherProjectId = other.getId();
        }
        entityManager.flush();
        return new Scenario(project.getId(), otherProjectId, gisu.getId(), chapter.getId(),
            round.getId(), form.getId(), response.getId(), answer.getId(), application.getId(), applicantId);
    }

    public void 자동선발완료(Long roundId) {
        entityManager.find(ProjectMatchingRound.class, roundId).executeAutoDecision(null);
    }

    public Long 지원기간(Long chapterId) {
        Instant now = Instant.now();
        ProjectMatchingRound round = ProjectMatchingRound.create("개발 2차", null,
            MatchingType.PLAN_DEVELOPER, MatchingPhase.SECOND, chapterId,
            now.minusSeconds(3600), now.plusSeconds(3600), now.plusSeconds(7200), null);
        entityManager.persist(round);
        return round.getId();
    }

    private Project 등록완료(Long gisuId, Long chapterId, Long ownerId) {
        Project project = Project.createDraft(gisuId, chapterId, ownerId, 1L, ownerId);
        project.updateBasicInfo("삭제 테스트 프로젝트", null, null, null, null);
        project.submit();
        project.publish();
        entityManager.persist(project);
        return project;
    }

    public record Scenario(Long projectId, Long otherProjectId, Long gisuId, Long chapterId,
                           Long roundId, Long formId, Long responseId, Long answerId, Long applicationId,
                           Long applicantId) {
    }
}
