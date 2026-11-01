package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationChapterPolicyTest {

    private static final Long GISU_ID = 11L;
    private static final Long SEOUL = 3L;
    private static final Long GYEONGGI = 5L;
    private static final Long APPLICANT_ID = 1L;

    @Mock
    ProjectMatchingChapterResolver matchingChapterResolver;

    @InjectMocks
    ProjectApplicationChapterPolicy sut;

    @Test
    @DisplayName("차수와 지원자 매칭 지부가 모두 프로젝트 지부와 같으면 통과한다")
    void 같은_지부면_통과() {
        Project project = project(GYEONGGI);
        given(matchingChapterResolver.isInMatchingChapter(APPLICANT_ID, GISU_ID, GYEONGGI)).willReturn(true);

        assertThatCode(() -> sut.validate(project, round(GYEONGGI), APPLICANT_ID)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다른 지부의 차수를 고르면 PROJECT_APPLICATION_ROUND_CHAPTER_MISMATCH")
    void 다른_지부_차수면_거부() {
        Project project = project(GYEONGGI);

        assertThatThrownBy(() -> sut.validate(project, round(SEOUL), APPLICANT_ID))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_ROUND_CHAPTER_MISMATCH);
    }

    @Test
    @DisplayName("지원자의 매칭 지부가 프로젝트 지부와 다르면 PROJECT_APPLICATION_CHAPTER_MISMATCH")
    void 다른_지부_지원자면_거부() {
        Project project = project(GYEONGGI);
        given(matchingChapterResolver.isInMatchingChapter(APPLICANT_ID, GISU_ID, GYEONGGI)).willReturn(false);

        assertThatThrownBy(() -> sut.validate(project, round(GYEONGGI), APPLICANT_ID))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_CHAPTER_MISMATCH);
    }

    private static Project project(Long chapterId) {
        return Project.createDraft(GISU_ID, chapterId, 50L, 30L, 50L);
    }

    private static ProjectMatchingRound round(Long chapterId) {
        Instant now = Instant.now();
        return ProjectMatchingRound.create(
            "디자인 1차", null, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, chapterId,
            now.minusSeconds(3600), now.plusSeconds(3600), now.plusSeconds(7200), now.plusSeconds(14400)
        );
    }
}
