package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.in.query.dto.ProjectApplicantMatchingRoundInfo;
import com.umc.product.project.application.port.out.LoadMatchingStatisticsPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMemberStatisticsPersistenceAdapter implements LoadMatchingStatisticsPort {

    private final ProjectMemberStatisticsQueryRepository queryRepository;

    @Override
    public List<ProjectApplicantMatchingRoundInfo> getMembersByRound(Long gisuId, Long chapterId) {
        return queryRepository.getMembersByRound(gisuId, chapterId);
    }

    @Override
    public List<ProjectApplicantMatchingRoundInfo> getMembersByRoundForOwner(Long ownerMemberId, Long gisuId,
                                                                             Long chapterId) {
        return queryRepository.getMembersByRoundForOwner(ownerMemberId, gisuId, chapterId);
    }
}
