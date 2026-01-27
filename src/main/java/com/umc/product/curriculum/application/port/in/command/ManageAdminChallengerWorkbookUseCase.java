package com.umc.product.curriculum.application.port.in.command;

public interface ManageAdminChallengerWorkbookUseCase {

    void review(ReviewWorkbookCommand command);

    void selectBest(SelectBestWorkbookCommand command);
}
