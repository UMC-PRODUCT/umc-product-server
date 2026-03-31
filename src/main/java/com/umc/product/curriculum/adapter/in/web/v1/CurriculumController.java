package com.umc.product.curriculum.adapter.in.web.v1;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.curriculum.adapter.in.web.v1.dto.request.SubmitChallengerWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.v1.dto.request.SubmitWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.v1.swagger.CurriculumControllerApi;
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
public class CurriculumController implements CurriculumControllerApi {

    private final ManageWorkbookUseCase manageWorkbookUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    @Deprecated(since = "1.3.0", forRemoval = true)
    @PostMapping("/submission")
    public void submitWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody SubmitWorkbookRequest request
    ) {
        ChallengerInfoWithStatus challenger = getChallengerUseCase
            .getLatestActiveChallengerByMemberId(memberPrincipal.getMemberId());

        manageWorkbookUseCase.submit(request.toCommand(challenger.challengerId()));
    }

    @Override
    @PostMapping("/{challengerWorkbookId}/submission")
    public void submitChallengerWorkbook(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long challengerWorkbookId,
        @Valid @RequestBody SubmitChallengerWorkbookRequest request
    ) {
        manageWorkbookUseCase.submitByWorkbookId(
            request.toCommand(challengerWorkbookId, memberPrincipal.getMemberId())
        );
    }
}
