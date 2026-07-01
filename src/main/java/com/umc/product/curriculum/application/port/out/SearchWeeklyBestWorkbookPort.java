package com.umc.product.curriculum.application.port.out;

import java.util.List;

import com.umc.product.curriculum.application.port.in.query.dto.GetBestWorkbooksQuery;
import com.umc.product.curriculum.domain.WeeklyBestWorkbook;

public interface SearchWeeklyBestWorkbookPort {

    List<WeeklyBestWorkbook> searchBestWorkbooks(GetBestWorkbooksQuery query);
}
