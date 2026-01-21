package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.in.query.GetScheduleListUseCase;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryService implements GetScheduleListUseCase {

    @Override
    public List<ScheduleWithStatsInfo> getAll() {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
