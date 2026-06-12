package com.umc.product.test.adapter.out.persistence;

import com.umc.product.test.application.port.out.DeleteSeedProjectDataPort;
import com.umc.product.test.application.port.out.dto.ProjectDataDeletionCounts;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ProjectSeedDataCleanupPersistenceAdapter implements DeleteSeedProjectDataPort {

    private final EntityManager entityManager;

    @Override
    public ProjectDataDeletionCounts deleteByGisuId(Long gisuId) {
        int deletedProjectMembers = execute("""
            DELETE FROM project_member pm
            WHERE pm.project_id IN (
                SELECT p.id
                FROM project p
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedSurveyAnswerChoices = execute("""
            DELETE FROM answer_choice ac
            WHERE ac.answer_id IN (
                SELECT a.id
                FROM answer a
                JOIN form_response fr ON fr.id = a.form_response_id
                JOIN project_application_form paf ON paf.form_id = fr.form_id
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedSurveySingleAnswers = execute("""
            DELETE FROM single_answer sa
            WHERE sa.response_id IN (
                SELECT fr.id
                FROM form_response fr
                JOIN project_application_form paf ON paf.form_id = fr.form_id
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedSurveyAnswers = execute("""
            DELETE FROM answer a
            WHERE a.form_response_id IN (
                SELECT fr.id
                FROM form_response fr
                JOIN project_application_form paf ON paf.form_id = fr.form_id
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedProjectApplications = execute("""
            DELETE FROM project_application pa
            WHERE pa.project_application_form_id IN (
                SELECT paf.id
                FROM project_application_form paf
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedProjectApplicationFormPolicies = execute("""
            DELETE FROM project_application_form_policy pfp
            WHERE pfp.project_application_form_id IN (
                SELECT paf.id
                FROM project_application_form paf
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedSurveyQuestionOptions = execute("""
            DELETE FROM question_option qo
            WHERE qo.question_id IN (
                SELECT q.id
                FROM question q
                JOIN form_section fs ON fs.id = q.form_section_id
                JOIN project_application_form paf ON paf.form_id = fs.form_id
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedSurveyQuestions = execute("""
            DELETE FROM question q
            WHERE q.form_section_id IN (
                SELECT fs.id
                FROM form_section fs
                JOIN project_application_form paf ON paf.form_id = fs.form_id
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedSurveyFormSections = execute("""
            DELETE FROM form_section fs
            WHERE fs.form_id IN (
                SELECT paf.form_id
                FROM project_application_form paf
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedSurveyFormResponses = execute("""
            DELETE FROM form_response fr
            WHERE fr.form_id IN (
                SELECT paf.form_id
                FROM project_application_form paf
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedSurveyForms = execute("""
            DELETE FROM form f
            WHERE f.id IN (
                SELECT paf.form_id
                FROM project_application_form paf
                JOIN project p ON p.id = paf.project_id
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedProjectApplicationForms = execute("""
            DELETE FROM project_application_form paf
            WHERE paf.project_id IN (
                SELECT p.id
                FROM project p
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedProjectPartQuotas = execute("""
            DELETE FROM project_part_quota ppq
            WHERE ppq.project_id IN (
                SELECT p.id
                FROM project p
                WHERE p.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedProjectMatchingRounds = execute("""
            DELETE FROM project_matching_round pmr
            WHERE pmr.chapter_id IN (
                SELECT c.id
                FROM chapter c
                WHERE c.gisu_id = :gisuId
            )
            """, gisuId);

        int deletedProjects = execute("""
            DELETE FROM project p
            WHERE p.gisu_id = :gisuId
            """, gisuId);

        return ProjectDataDeletionCounts.builder()
            .deletedProjects(deletedProjects)
            .deletedProjectMembers(deletedProjectMembers)
            .deletedProjectApplications(deletedProjectApplications)
            .deletedProjectApplicationForms(deletedProjectApplicationForms)
            .deletedProjectApplicationFormPolicies(deletedProjectApplicationFormPolicies)
            .deletedProjectPartQuotas(deletedProjectPartQuotas)
            .deletedProjectMatchingRounds(deletedProjectMatchingRounds)
            .deletedSurveyForms(deletedSurveyForms)
            .deletedSurveyFormSections(deletedSurveyFormSections)
            .deletedSurveyQuestions(deletedSurveyQuestions)
            .deletedSurveyQuestionOptions(deletedSurveyQuestionOptions)
            .deletedSurveyFormResponses(deletedSurveyFormResponses)
            .deletedSurveyAnswers(deletedSurveyAnswers)
            .deletedSurveyAnswerChoices(deletedSurveyAnswerChoices)
            .deletedSurveySingleAnswers(deletedSurveySingleAnswers)
            .build();
    }

    private int execute(String sql, Long gisuId) {
        return entityManager.createNativeQuery(sql)
            .setParameter("gisuId", gisuId)
            .executeUpdate();
    }
}
