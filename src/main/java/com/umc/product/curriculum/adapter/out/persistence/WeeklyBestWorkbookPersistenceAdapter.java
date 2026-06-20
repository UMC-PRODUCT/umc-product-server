package com.umc.product.curriculum.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.curriculum.application.port.in.query.dto.GetBestWorkbooksQuery;
import com.umc.product.curriculum.application.port.out.SearchWeeklyBestWorkbookPort;
import com.umc.product.curriculum.domain.WeeklyBestWorkbook;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WeeklyBestWorkbookPersistenceAdapter implements SearchWeeklyBestWorkbookPort {

    private final WeeklyBestWorkbookQueryRepository weeklyBestWorkbookQueryRepository;

    @Override
    public List<WeeklyBestWorkbook> searchBestWorkbooks(GetBestWorkbooksQuery query) {
        return weeklyBestWorkbookQueryRepository.searchBestWorkbooks(query);
    }
}
