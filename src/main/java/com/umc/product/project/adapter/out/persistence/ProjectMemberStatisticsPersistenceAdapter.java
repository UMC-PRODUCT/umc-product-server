package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.in.query.dto.RoundMemberInfo;
import com.umc.product.project.application.port.out.LoadMatchingStatisticsPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMemberStatisticsPersistenceAdapter implements LoadMatchingStatisticsPort {

    private final ProjectMemberStatisticsQueryRepository queryRepository;

    @Override
    public List<RoundMemberInfo> getMembersByRound(Long gisuId, Long chapterId) {
        return queryRepository.getMembersByRound(gisuId, chapterId);
    }

    @Override
    public List<RoundMemberInfo> getMembersByRoundForOwner(Long ownerMemberId, Long gisuId, Long chapterId) {
        return queryRepository.getMembersByRoundForOwner(ownerMemberId, gisuId, chapterId);
    }
}
