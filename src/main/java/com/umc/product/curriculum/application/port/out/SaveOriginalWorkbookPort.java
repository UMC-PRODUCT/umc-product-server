package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.util.List;

public interface SaveOriginalWorkbookPort {

    OriginalWorkbook save(OriginalWorkbook workbook);

    List<OriginalWorkbook> saveAll(List<OriginalWorkbook> workbooks);

    void delete(OriginalWorkbook workbook);
}
