package com.umc.product.project.application.service.query;

import com.umc.product.project.application.port.in.query.GetProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Map<Long, ProjectMatchingRoundInfo> findAllByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<ProjectMatchingRound> rounds = loadProjectMatchingRoundPort.listByIds(List.copyOf(ids));
        Map<Long, ProjectMatchingRoundInfo> result = new LinkedHashMap<>();
        for (ProjectMatchingRound round : rounds) {
            result.put(round.getId(), ProjectMatchingRoundInfo.from(round));
        }
        return result;
    }
}
