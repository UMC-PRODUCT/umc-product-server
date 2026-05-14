package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import com.umc.product.project.application.port.out.LoadApplicationStatisticsPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectApplicationStatisticsPersistenceAdapter implements LoadApplicationStatisticsPort {

    private final ProjectApplicationStatisticsQueryRepository queryRepository;

    @Override
    public List<RoundMemberInfo> listApplicantsByRound(Long gisuId, Long chapterId) {
        return queryRepository.listApplicantsByRound(gisuId, chapterId);
    }

    @Override
    public List<RoundMemberInfo> listApplicantsByRoundForProject(Long projectId) {
        return queryRepository.listApplicantsByRoundForProject(projectId);
    }
}
