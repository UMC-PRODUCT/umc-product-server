package com.umc.product.curriculum.application.service.query;

import com.umc.product.curriculum.application.port.in.query.GetWeeklyBestWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.GetBestWorkbooksQuery;
import com.umc.product.curriculum.application.port.in.query.dto.WeeklyBestWorkbookInfo;
import com.umc.product.global.exception.NotImplementedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyBestWorkbookQueryService implements GetWeeklyBestWorkbookUseCase {

    @Override
    public List<WeeklyBestWorkbookInfo> searchBestWorkbooks(GetBestWorkbooksQuery query) {
        throw new NotImplementedException();
    }
}