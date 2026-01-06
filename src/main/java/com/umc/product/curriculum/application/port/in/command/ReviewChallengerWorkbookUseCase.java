package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.dto.ReviewWorkbookCommand;

public interface ReviewChallengerWorkbookUseCase {
    void review(ReviewWorkbookCommand command);
}
