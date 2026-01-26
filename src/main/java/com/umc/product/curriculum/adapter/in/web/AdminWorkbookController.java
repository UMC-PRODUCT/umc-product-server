package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.curriculum.application.port.in.command.ReleaseWorkbookUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/workbooks")
@RequiredArgsConstructor
public class AdminWorkbookController implements AdminWorkbookControllerApi {

    private final ReleaseWorkbookUseCase releaseWorkbookUseCase;

    @Override
    @PostMapping("/{workbookId}/release")
    public void releaseWorkbook(@PathVariable Long workbookId) {
        releaseWorkbookUseCase.release(workbookId);
    }
}
