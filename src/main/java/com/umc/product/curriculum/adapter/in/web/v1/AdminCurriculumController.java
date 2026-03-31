package com.umc.product.curriculum.adapter.in.web.v1;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.curriculum.adapter.in.web.v1.dto.request.ReviewWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.v1.dto.request.SelectBestWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.v1.dto.response.WorkbookSubmissionDetailResponse;
import com.umc.product.curriculum.adapter.in.web.v1.swagger.AdminCurriculumControllerApi;
import com.umc.product.curriculum.application.port.in.command.ManageChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.GetChallengerWorkbookUseCase;
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
public class AdminCurriculumController implements AdminCurriculumControllerApi {

    private final ManageChallengerWorkbookUseCase manageChallengerWorkbookUseCase;
    private final ManageOriginalWorkbookUseCase manageOriginalWorkbookUseCase;
    private final GetChallengerWorkbookUseCase getChallengerWorkbookUseCase;

    @Override
    @CheckAccess(
        resourceType = ResourceType.ORIGINAL_WORKBOOK,
        permission = PermissionType.RELEASE,
        message = "워크북 배포는 중앙운영사무국 교육국 소속 파트장만 가능합니다."
    )
    @PostMapping("/{workbookId}/release")
    public void releaseWorkbook(@PathVariable Long workbookId) {
        manageOriginalWorkbookUseCase.release(workbookId);
    }

    @Override
    @PostMapping("/challenger/{challengerWorkbookId}/review")
    public void reviewWorkbook(
        @PathVariable Long challengerWorkbookId,
        @Valid @RequestBody ReviewWorkbookRequest request) {
        manageChallengerWorkbookUseCase.review(request.toCommand(challengerWorkbookId));
    }

    @Override
    @PatchMapping("/challenger/{challengerWorkbookId}/best")
    public void selectBestWorkbook(
        @PathVariable Long challengerWorkbookId,
        @Valid @RequestBody SelectBestWorkbookRequest request) {
        manageChallengerWorkbookUseCase.selectBest(request.toCommand(challengerWorkbookId));
    }

    @Override
    @GetMapping("/challenger/{challengerWorkbookId}/submissions")
    public WorkbookSubmissionDetailResponse getSubmissionDetail(
        @PathVariable Long challengerWorkbookId) {
        return WorkbookSubmissionDetailResponse.from(
            getChallengerWorkbookUseCase.getSubmissionDetail(challengerWorkbookId)
        );
    }
}
