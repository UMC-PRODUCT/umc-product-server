package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.domain.OriginalWorkbook;

public interface SaveOriginalWorkbookPort {

    OriginalWorkbook save(OriginalWorkbook workbook);
}
