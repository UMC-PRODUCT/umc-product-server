package com.umc.product.test.application.port.in.command.dto;

import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.test.domain.startingline.StartingLineStep;

/**
 * 출발선 실행 결과. 프로젝트마다 이번 호출에 실행한 단계와, 실패했다면 그 단계와 운영 에러 코드를 담는다.
 */
public record StartingLineResult(List<ChapterLine> chapters, List<ProjectOutcome> projects) {
    public StartingLineResult {
        chapters = List.copyOf(chapters);
        projects = List.copyOf(projects);
    }

    /**
     * @param reservedForQa 지부 매칭 인원 중 시딩 프로젝트에 나누지 않고 QA 몫으로 남긴 TO
     */
    public record ChapterLine(
        String chapter,
        List<String> withoutProject,
        int qaProjects,
        int seededProjects,
        Map<ChallengerPart, Integer> quotaTotals,
        Map<ChallengerPart, Integer> reservedForQa
    ) {
        public ChapterLine {
            withoutProject = List.copyOf(withoutProject);
            quotaTotals = Map.copyOf(quotaTotals);
            reservedForQa = Map.copyOf(reservedForQa);
        }
    }

    public record ProjectOutcome(
        String chapter,
        String owner,
        Role role,
        Long projectId,
        ProjectStatus status,
        List<StartingLineStep> applied,
        StartingLineStep failedStep,
        String errorCode,
        String message
    ) {
        public ProjectOutcome {
            applied = List.copyOf(applied);
        }
    }

    public enum Role {
        QA,
        SEEDED
    }
}
