package com.umc.product.project.application.service.policy;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 프로젝트 지원의 지부 일치 규칙입니다.
 * <ul>
 *   <li>지원하는 매칭 차수는 프로젝트 지부의 차수여야 한다.</li>
 *   <li>지원자의 매칭 지부({@link ProjectMatchingChapterResolver})가 프로젝트 지부와 같아야 한다.</li>
 * </ul>
 * 지원서 생성·제출과 지원 가능 여부 플래그가 모두 이 규칙을 사용합니다. 다른 지부 프로젝트 지원을 허용하는 예외가 생기면
 * 이 클래스에만 추가합니다.
 */
@Component
@RequiredArgsConstructor
public class ProjectApplicationChapterPolicy {

    private final ProjectMatchingChapterResolver matchingChapterResolver;

    /**
     * 지원서 생성·제출 시 지부 일치 규칙을 검증합니다.
     */
    public void validate(Project project, ProjectMatchingRound round, Long applicantMemberId) {
        if (!isRoundInProjectChapter(project, round)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_ROUND_CHAPTER_MISMATCH);
        }
        if (!isApplicantInProjectChapter(project, applicantMemberId)) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_CHAPTER_MISMATCH);
        }
    }

    /**
     * 지원자의 매칭 지부가 프로젝트 지부와 같은지 확인합니다.
     */
    public boolean isApplicantInProjectChapter(Project project, Long applicantMemberId) {
        return matchingChapterResolver.isInMatchingChapter(
            applicantMemberId, project.getGisuId(), project.getChapterId()
        );
    }

    private boolean isRoundInProjectChapter(Project project, ProjectMatchingRound round) {
        return Objects.equals(round.getChapterId(), project.getChapterId());
    }
}
