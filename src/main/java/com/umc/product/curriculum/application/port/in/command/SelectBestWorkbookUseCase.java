package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.dto.SelectBestWorkbookCommand;

public interface SelectBestWorkbookUseCase {
    void select(SelectBestWorkbookCommand command);
}
