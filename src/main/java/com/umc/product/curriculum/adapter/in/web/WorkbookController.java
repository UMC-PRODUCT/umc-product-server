package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.curriculum.adapter.in.web.dto.request.SubmitWorkbookRequest;
import com.umc.product.curriculum.application.port.in.command.ManageWorkbookUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger-workbooks")
@RequiredArgsConstructor
public class WorkbookController implements WorkbookControllerApi {

    private final ManageWorkbookUseCase manageWorkbookUseCase;

    @Override
    @PostMapping("/{challengerWorkbookId}/submissions")
    public void submitWorkbook(
            @PathVariable Long challengerWorkbookId,
            @Valid @RequestBody SubmitWorkbookRequest request) {
        manageWorkbookUseCase.submit(request.toCommand(challengerWorkbookId));
    }
}
