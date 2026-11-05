package com.umc.product.test.adapter.in.web.dto;

import java.util.List;

import com.umc.product.test.application.port.in.command.dto.StartingLineResult;
import com.umc.product.test.application.port.in.command.dto.StartingLineResult.ChapterLine;
import com.umc.product.test.application.port.in.command.dto.StartingLineResult.ProjectOutcome;

/**
 * @param applied   이번 호출에 단계를 하나 이상 실행한 프로젝트 수
 * @param unchanged 이미 목표 상태라 건너뛴 프로젝트 수
 * @param failed    단계가 실패한 프로젝트 수. 다시 부르면 실패한 단계부터 이어서 한다
 */
public record StartingLineResponse(
    int applied,
    int unchanged,
    int failed,
    List<ChapterLine> chapters,
    List<ProjectOutcome> projects
) {
    public static StartingLineResponse from(StartingLineResult result) {
        int failed = (int) result.projects().stream().filter(project -> project.failedStep() != null).count();
        int applied = (int) result.projects().stream()
            .filter(project -> project.failedStep() == null && !project.applied().isEmpty()).count();
        return new StartingLineResponse(applied, result.projects().size() - applied - failed, failed,
            result.chapters(), result.projects());
    }
}
