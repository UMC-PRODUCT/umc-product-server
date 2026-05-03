package com.umc.product.project.application.service.query;

import com.umc.product.project.application.port.in.query.GetProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMatchingRoundQueryService implements GetProjectMatchingRoundUseCase {

    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;

    @Override
    public List<ProjectMatchingRoundInfo> list(Long chapterId, Instant time) {
        List<ProjectMatchingRound> rounds;
        if (time != null) {
            if (chapterId == null) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_MATCHING_ROUND_TIME_REQUIRES_CHAPTER);
            }
            rounds = loadProjectMatchingRoundPort.listOpenAt(chapterId, time);
        } else if (chapterId != null) {
            rounds = loadProjectMatchingRoundPort.listByChapterId(chapterId);
        } else {
            rounds = loadProjectMatchingRoundPort.listAll();
        }

        return rounds.stream()
            .map(ProjectMatchingRoundInfo::from)
            .toList();
    }
}
