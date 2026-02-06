package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.curriculum.adapter.in.web.dto.request.ReviewWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.dto.request.SelectBestWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.dto.response.WorkbookSubmissionDetailResponse;
import com.umc.product.curriculum.application.port.in.command.ManageWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ReleaseWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.GetWorkbookSubmissionsUseCase;
import com.umc.product.global.security.annotation.Public;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workbooks")
@RequiredArgsConstructor
public class AdminWorkbookController implements AdminWorkbookControllerApi {

    private final ReleaseWorkbookUseCase releaseWorkbookUseCase;
    private final ManageWorkbookUseCase manageWorkbookUseCase;
    private final GetWorkbookSubmissionsUseCase getWorkbookSubmissionsUseCase;

    @Public
    @Override
    @PostMapping("/{workbookId}/release")
    public void releaseWorkbook(@PathVariable Long workbookId) {
        // TODO: user의 권한에 따라 막히게 구현 필요
        releaseWorkbookUseCase.release(workbookId);
    }

    @Override
    @PostMapping("/challenger/{challengerWorkbookId}/review")
    public void reviewWorkbook(
            @PathVariable Long challengerWorkbookId,
            @Valid @RequestBody ReviewWorkbookRequest request) {
        manageWorkbookUseCase.review(request.toCommand(challengerWorkbookId));
    }

    @Override
    @PatchMapping("/challenger/{challengerWorkbookId}/best")
    public void selectBestWorkbook(
            @PathVariable Long challengerWorkbookId,
            @Valid @RequestBody SelectBestWorkbookRequest request) {
        manageWorkbookUseCase.selectBest(request.toCommand(challengerWorkbookId));
    }

    @Override
    @GetMapping("/challenger/{challengerWorkbookId}/submissions")
    public WorkbookSubmissionDetailResponse getSubmissionDetail(
            @PathVariable Long challengerWorkbookId) {
        return WorkbookSubmissionDetailResponse.from(
                getWorkbookSubmissionsUseCase.getSubmissionDetail(challengerWorkbookId)
        );
    }
}
