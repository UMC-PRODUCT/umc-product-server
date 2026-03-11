package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.curriculum.adapter.in.web.dto.request.SubmitWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.swagger.WorkbookControllerApi;
import com.umc.product.curriculum.application.port.in.command.ManageWorkbookUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workbooks")
@RequiredArgsConstructor
public class WorkbookController implements WorkbookControllerApi {

    private final ManageWorkbookUseCase manageWorkbookUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    @PostMapping("/{originalWorkbookId}/submissions")
    public void submitWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long originalWorkbookId,
        @Valid @RequestBody SubmitWorkbookRequest request) {
        ChallengerInfoWithStatus challenger = getChallengerUseCase
            .getLatestActiveChallengerByMemberId(memberPrincipal.getMemberId());
        manageWorkbookUseCase.submit(request.toCommand(originalWorkbookId, challenger.challengerId()));
    }
}
