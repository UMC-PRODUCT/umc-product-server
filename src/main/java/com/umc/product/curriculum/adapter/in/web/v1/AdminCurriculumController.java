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
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Base path: /api/v1/curriculum 으로 나중에 예정
@RestController
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
    @PostMapping("/api/v1/curriculum/original-workbooks/{workbookId}/release")
    public void releaseWorkbook(@PathVariable Long workbookId) {
        manageOriginalWorkbookUseCase.release(workbookId);
    }

    // ── 신규 경로 ──

    @Override
    @PostMapping("/api/v1/curriculum/challenger-workbooks/{challengerWorkbookId}/reviews")
    public void reviewWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerWorkbookId,
        @Valid @RequestBody ReviewWorkbookRequest request) {
        manageChallengerWorkbookUseCase.review(
            request.toCommand(challengerWorkbookId, memberPrincipal.getMemberId()));
    }

    @Override
    @PatchMapping("/api/v1/curriculum/challenger-workbooks/{challengerWorkbookId}/best")
    public void selectBestWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerWorkbookId,
        @Valid @RequestBody SelectBestWorkbookRequest request) {
        manageChallengerWorkbookUseCase.selectBest(
            request.toCommand(challengerWorkbookId, memberPrincipal.getMemberId()));
    }

    @Override
    @GetMapping("/api/v1/curriculum/challenger-workbooks/{challengerWorkbookId}/submissions")
    public WorkbookSubmissionDetailResponse getSubmissionDetail(
        @PathVariable Long challengerWorkbookId) {
        return WorkbookSubmissionDetailResponse.from(
            getChallengerWorkbookUseCase.getSubmissionDetail(challengerWorkbookId)
        );
    }

    // ── Deprecated 경로 (삭제 예정) ──

    @Deprecated
    @Operation(summary = "(파트장용) 챌린저 워크북 검토", deprecated = true)
    @PostMapping("/api/v1/workbooks/challenger/{challengerWorkbookId}/review")
    public void reviewWorkbookLegacy(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerWorkbookId,
        @Valid @RequestBody ReviewWorkbookRequest request) {
        reviewWorkbook(memberPrincipal, challengerWorkbookId, request);
    }

    @Deprecated
    @Operation(summary = "(파트장용) 베스트 워크북 선정", deprecated = true)
    @PatchMapping("/api/v1/workbooks/challenger/{challengerWorkbookId}/best")
    public void selectBestWorkbookLegacy(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerWorkbookId,
        @Valid @RequestBody SelectBestWorkbookRequest request) {
        selectBestWorkbook(memberPrincipal, challengerWorkbookId, request);
    }

    @Deprecated
    @Operation(summary = "(파트장용) 챌린저 워크북 제출 URL 조회", deprecated = true)
    @GetMapping("/api/v1/workbooks/challenger/{challengerWorkbookId}/submissions")
    public WorkbookSubmissionDetailResponse getSubmissionDetailLegacy(
        @PathVariable Long challengerWorkbookId) {
        return getSubmissionDetail(challengerWorkbookId);
    }
}
