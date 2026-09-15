package com.umc.product.curriculum.application.port.out;

import java.util.List;

import com.umc.product.curriculum.domain.OriginalWorkbook;

public interface SaveOriginalWorkbookPort {

    OriginalWorkbook save(OriginalWorkbook workbook);

    List<OriginalWorkbook> saveAll(List<OriginalWorkbook> workbooks);

    void delete(OriginalWorkbook workbook);
}
