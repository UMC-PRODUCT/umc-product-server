package com.umc.product.test.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.support.TestContainersConfig;
import com.umc.product.test.application.port.out.dto.ProjectDataDeletionCounts;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "app.seed.enabled=true")
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    TestContainersConfig.class,
    ProjectSeedDataCleanupPersistenceAdapter.class
})
class ProjectSeedDataCleanupPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProjectSeedDataCleanupPersistenceAdapter sut;

    @Test
    @DisplayName("대상 기수의 프로젝트 관련 데이터만 모두 삭제한다")
    void 대상_기수_프로젝트_관련_데이터만_삭제() {
        // Given
        Long targetGisuId = insertGisu(11L);
        Long otherGisuId = insertGisu(12L);
        Long targetChapterId = insertChapter(targetGisuId, "target");
        Long otherChapterId = insertChapter(otherGisuId, "other");
        ProjectGraph target = insertProjectGraph(targetGisuId, targetChapterId, 100L);
        ProjectGraph other = insertProjectGraph(otherGisuId, otherChapterId, 200L);
        em.flush();
        em.clear();

        // When
        ProjectDataDeletionCounts result = sut.deleteByGisuId(targetGisuId);
        em.flush();
        em.clear();

        // Then
        assertThat(result.deletedProjects()).isEqualTo(1);
        assertThat(result.deletedProjectMembers()).isEqualTo(1);
        assertThat(result.deletedProjectApplications()).isEqualTo(1);
        assertThat(result.deletedProjectApplicationForms()).isEqualTo(1);
        assertThat(result.deletedProjectApplicationFormPolicies()).isEqualTo(1);
        assertThat(result.deletedProjectPartQuotas()).isEqualTo(1);
        assertThat(result.deletedProjectMatchingRounds()).isEqualTo(1);
        assertThat(result.deletedSurveyForms()).isEqualTo(1);
        assertThat(result.deletedSurveyFormSections()).isEqualTo(1);
        assertThat(result.deletedSurveyQuestions()).isEqualTo(1);
        assertThat(result.deletedSurveyQuestionOptions()).isEqualTo(1);
        assertThat(result.deletedSurveyFormResponses()).isEqualTo(1);
        assertThat(result.deletedSurveyAnswers()).isEqualTo(1);
        assertThat(result.deletedSurveyAnswerChoices()).isEqualTo(1);
        assertThat(result.deletedSurveySingleAnswers()).isEqualTo(1);

        assertGraphDeleted(target);
        assertGraphExists(other);
    }

    private ProjectGraph insertProjectGraph(Long gisuId, Long chapterId, Long seed) {
        Long projectId = insertProject(gisuId, chapterId, seed);
        Long roundId = insertMatchingRound(chapterId, seed);
        Long formId = insertForm(seed);
        Long sectionId = insertFormSection(formId, seed);
        Long questionId = insertQuestion(sectionId, seed);
        Long optionId = insertQuestionOption(questionId, seed);
        Long formResponseId = insertFormResponse(formId, seed);
        Long answerId = insertAnswer(formResponseId, questionId);
        Long answerChoiceId = insertAnswerChoice(answerId, optionId);
        Long singleAnswerId = insertSingleAnswer(formResponseId, questionId, optionId);
        Long applicationFormId = insertProjectApplicationForm(projectId, formId);
        Long policyId = insertProjectApplicationFormPolicy(applicationFormId, sectionId);
        Long applicationId = insertProjectApplication(applicationFormId, formResponseId, roundId, seed);
        Long memberId = insertProjectMember(projectId, applicationId, seed);
        Long quotaId = insertProjectPartQuota(projectId, seed);

        return new ProjectGraph(
            projectId,
            memberId,
            quotaId,
            roundId,
            applicationId,
            applicationFormId,
            policyId,
            formId,
            sectionId,
            questionId,
            optionId,
            formResponseId,
            answerId,
            answerChoiceId,
            singleAnswerId
        );
    }

    private Long insertGisu(Long generation) {
        return insertReturningId("""
            INSERT INTO gisu (created_at, updated_at, generation, start_at, end_at, is_active)
            VALUES (now(), now(), :generation, :startAt, :endAt, false)
            RETURNING id
            """, query -> {
            query.setParameter("generation", generation);
            query.setParameter("startAt", Instant.parse("2026-01-01T00:00:00Z"));
            query.setParameter("endAt", Instant.parse("2026-12-31T00:00:00Z"));
        });
    }

    private Long insertChapter(Long gisuId, String suffix) {
        return insertReturningId("""
            INSERT INTO chapter (created_at, updated_at, gisu_id, name)
            VALUES (now(), now(), :gisuId, :name)
            RETURNING id
            """, query -> {
            query.setParameter("gisuId", gisuId);
            query.setParameter("name", "chapter-" + suffix);
        });
    }

    private Long insertProject(Long gisuId, Long chapterId, Long seed) {
        return insertReturningId("""
            INSERT INTO project (
                created_at, updated_at, gisu_id, chapter_id, status, name,
                product_owner_member_id, product_owner_school_id, created_by_member_id
            )
            VALUES (
                now(), now(), :gisuId, :chapterId, 'IN_PROGRESS', :name,
                :ownerId, :schoolId, :ownerId
            )
            RETURNING id
            """, query -> {
            query.setParameter("gisuId", gisuId);
            query.setParameter("chapterId", chapterId);
            query.setParameter("name", "project-" + seed);
            query.setParameter("ownerId", seed);
            query.setParameter("schoolId", seed);
        });
    }

    private Long insertMatchingRound(Long chapterId, Long seed) {
        return insertReturningId("""
            INSERT INTO project_matching_round (
                created_at, updated_at, name, description, type, phase, chapter_id,
                starts_at, ends_at, decision_deadline
            )
            VALUES (
                now(), now(), :name, null, 'PLAN_DESIGN', 'FIRST', :chapterId,
                now() - interval '1 hour', now() + interval '1 hour', now() + interval '2 hours'
            )
            RETURNING id
            """, query -> {
            query.setParameter("chapterId", chapterId);
            query.setParameter("name", "round-" + seed);
        });
    }

    private Long insertForm(Long seed) {
        return insertReturningId("""
            INSERT INTO form (created_at, updated_at, created_member_id, title, status, is_anonymous)
            VALUES (now(), now(), :memberId, :title, 'PUBLISHED', false)
            RETURNING id
            """, query -> {
            query.setParameter("memberId", seed);
            query.setParameter("title", "form-" + seed);
        });
    }

    private Long insertFormSection(Long formId, Long seed) {
        return insertReturningId("""
            INSERT INTO form_section (created_at, updated_at, form_id, title, description, order_no)
            VALUES (now(), now(), :formId, :title, null, 1)
            RETURNING id
            """, query -> {
            query.setParameter("formId", formId);
            query.setParameter("title", "section-" + seed);
        });
    }

    private Long insertQuestion(Long sectionId, Long seed) {
        return insertReturningId("""
            INSERT INTO question (
                created_at, updated_at, form_section_id, title, description, type,
                is_required, order_no, parent_question_id, is_active
            )
            VALUES (now(), now(), :sectionId, :title, null, 'RADIO', true, 1, null, true)
            RETURNING id
            """, query -> {
            query.setParameter("sectionId", sectionId);
            query.setParameter("title", "question-" + seed);
        });
    }

    private Long insertQuestionOption(Long questionId, Long seed) {
        return insertReturningId("""
            INSERT INTO question_option (created_at, updated_at, question_id, content, order_no, is_other)
            VALUES (now(), now(), :questionId, :content, 1, false)
            RETURNING id
            """, query -> {
            query.setParameter("questionId", questionId);
            query.setParameter("content", "option-" + seed);
        });
    }

    private Long insertFormResponse(Long formId, Long seed) {
        return insertReturningId("""
            INSERT INTO form_response (
                created_at, updated_at, form_id, respondent_member_id, status, last_saved_at
            )
            VALUES (now(), now(), :formId, :memberId, 'SUBMITTED', now())
            RETURNING id
            """, query -> {
            query.setParameter("formId", formId);
            query.setParameter("memberId", seed + 1000);
        });
    }

    private Long insertAnswer(Long formResponseId, Long questionId) {
        return insertReturningId("""
            INSERT INTO answer (
                created_at, updated_at, form_response_id, question_id, answered_as_type
            )
            VALUES (now(), now(), :formResponseId, :questionId, 'RADIO')
            RETURNING id
            """, query -> {
            query.setParameter("formResponseId", formResponseId);
            query.setParameter("questionId", questionId);
        });
    }

    private Long insertAnswerChoice(Long answerId, Long optionId) {
        return insertReturningId("""
            INSERT INTO answer_choice (
                created_at, updated_at, answer_id, answered_as_content, question_option_id
            )
            VALUES (now(), now(), :answerId, '선택지', :optionId)
            RETURNING id
            """, query -> {
            query.setParameter("answerId", answerId);
            query.setParameter("optionId", optionId);
        });
    }

    private Long insertSingleAnswer(Long responseId, Long questionId, Long optionId) {
        return insertReturningId("""
            INSERT INTO single_answer (
                created_at, updated_at, response_id, question_id, answered_as_type, value
            )
            VALUES (
                now(), now(), :responseId, :questionId, 'RADIO',
                jsonb_build_object('selectedOptionId', :optionId)
            )
            RETURNING id
            """, query -> {
            query.setParameter("responseId", responseId);
            query.setParameter("questionId", questionId);
            query.setParameter("optionId", optionId);
        });
    }

    private Long insertProjectApplicationForm(Long projectId, Long formId) {
        return insertReturningId("""
            INSERT INTO project_application_form (created_at, updated_at, project_id, form_id)
            VALUES (now(), now(), :projectId, :formId)
            RETURNING id
            """, query -> {
            query.setParameter("projectId", projectId);
            query.setParameter("formId", formId);
        });
    }

    private Long insertProjectApplicationFormPolicy(Long applicationFormId, Long sectionId) {
        return insertReturningId("""
            INSERT INTO project_application_form_policy (
                created_at, updated_at, project_application_form_id, form_section_id, type, allowed_parts
            )
            VALUES (now(), now(), :applicationFormId, :sectionId, 'COMMON', null)
            RETURNING id
            """, query -> {
            query.setParameter("applicationFormId", applicationFormId);
            query.setParameter("sectionId", sectionId);
        });
    }

    private Long insertProjectApplication(
        Long applicationFormId, Long formResponseId, Long roundId, Long seed
    ) {
        return insertReturningId("""
            INSERT INTO project_application (
                created_at, updated_at, project_application_form_id, form_response_id,
                applicant_member_id, applied_matching_round_id, status, submitted_at, status_changed_at
            )
            VALUES (
                now(), now(), :applicationFormId, :formResponseId,
                :memberId, :roundId, 'APPROVED', now(), now()
            )
            RETURNING id
            """, query -> {
            query.setParameter("applicationFormId", applicationFormId);
            query.setParameter("formResponseId", formResponseId);
            query.setParameter("memberId", seed + 1000);
            query.setParameter("roundId", roundId);
        });
    }

    private Long insertProjectMember(Long projectId, Long applicationId, Long seed) {
        return insertReturningId("""
            INSERT INTO project_member (
                created_at, updated_at, project_id, project_application_id, member_id,
                part, is_leader, decided_member_id, decided_at, status
            )
            VALUES (
                now(), now(), :projectId, :applicationId, :memberId,
                'WEB', false, :decidedMemberId, now(), 'ACTIVE'
            )
            RETURNING id
            """, query -> {
            query.setParameter("projectId", projectId);
            query.setParameter("applicationId", applicationId);
            query.setParameter("memberId", seed + 1000);
            query.setParameter("decidedMemberId", seed);
        });
    }

    private Long insertProjectPartQuota(Long projectId, Long seed) {
        return insertReturningId("""
            INSERT INTO project_part_quota (
                created_at, updated_at, project_id, part, quota, last_edited_member_id
            )
            VALUES (now(), now(), :projectId, 'WEB', 3, :memberId)
            RETURNING id
            """, query -> {
            query.setParameter("projectId", projectId);
            query.setParameter("memberId", seed);
        });
    }

    private Long insertReturningId(String sql, QueryBinder binder) {
        var query = em.getEntityManager().createNativeQuery(sql);
        binder.bind(query);
        return ((Number) query.getSingleResult()).longValue();
    }

    private boolean existsById(String tableName, Long id) {
        Object result = em.getEntityManager()
            .createNativeQuery("SELECT EXISTS (SELECT 1 FROM " + tableName + " WHERE id = :id)")
            .setParameter("id", id)
            .getSingleResult();
        return (Boolean) result;
    }

    private void assertGraphDeleted(ProjectGraph graph) {
        assertGraphState(graph, false);
    }

    private void assertGraphExists(ProjectGraph graph) {
        assertGraphState(graph, true);
    }

    private void assertGraphState(ProjectGraph graph, boolean expected) {
        assertThat(existsById("project", graph.projectId())).isEqualTo(expected);
        assertThat(existsById("project_member", graph.memberId())).isEqualTo(expected);
        assertThat(existsById("project_part_quota", graph.quotaId())).isEqualTo(expected);
        assertThat(existsById("project_matching_round", graph.roundId())).isEqualTo(expected);
        assertThat(existsById("project_application", graph.applicationId())).isEqualTo(expected);
        assertThat(existsById("project_application_form", graph.applicationFormId())).isEqualTo(expected);
        assertThat(existsById("project_application_form_policy", graph.policyId())).isEqualTo(expected);
        assertThat(existsById("form", graph.formId())).isEqualTo(expected);
        assertThat(existsById("form_section", graph.sectionId())).isEqualTo(expected);
        assertThat(existsById("question", graph.questionId())).isEqualTo(expected);
        assertThat(existsById("question_option", graph.optionId())).isEqualTo(expected);
        assertThat(existsById("form_response", graph.formResponseId())).isEqualTo(expected);
        assertThat(existsById("answer", graph.answerId())).isEqualTo(expected);
        assertThat(existsById("answer_choice", graph.answerChoiceId())).isEqualTo(expected);
        assertThat(existsById("single_answer", graph.singleAnswerId())).isEqualTo(expected);
    }

    private interface QueryBinder {
        void bind(jakarta.persistence.Query query);
    }

    private record ProjectGraph(
        Long projectId,
        Long memberId,
        Long quotaId,
        Long roundId,
        Long applicationId,
        Long applicationFormId,
        Long policyId,
        Long formId,
        Long sectionId,
        Long questionId,
        Long optionId,
        Long formResponseId,
        Long answerId,
        Long answerChoiceId,
        Long singleAnswerId
    ) {
    }
}
