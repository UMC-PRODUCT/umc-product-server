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
    public List<RoundMemberInfo> listMembersByRound(Long gisuId, Long chapterId) {
        return queryRepository.listMembersByRound(gisuId, chapterId);
    }
}
