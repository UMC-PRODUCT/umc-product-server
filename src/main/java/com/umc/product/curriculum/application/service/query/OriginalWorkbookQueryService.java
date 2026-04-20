package com.umc.product.curriculum.application.service.query;

import com.umc.product.curriculum.application.port.in.query.GetOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.OriginalWorkbookInfo;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OriginalWorkbookQueryService implements GetOriginalWorkbookUseCase {

    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;

    @Override
    public OriginalWorkbookInfo getById(Long originalWorkbookId) {
        throw new NotImplementedException();
    }

}
