package com.umc.product.curriculum.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OriginalWorkbookQueryService implements GetOriginalWorkbookUseCase {

    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;

    @Override
    public List<Integer> getAvailableWeeks(ChallengerPart part) {
        return loadOriginalWorkbookPort.findReleasedWeekNos(part);
    }
}
