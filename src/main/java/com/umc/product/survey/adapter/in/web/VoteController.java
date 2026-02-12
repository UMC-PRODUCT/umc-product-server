package com.umc.product.survey.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.survey.adapter.in.web.dto.request.CreateVoteRequest;
import com.umc.product.survey.adapter.in.web.dto.response.CreateVoteResponse;
import com.umc.product.survey.application.port.in.command.CreateVoteUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateVoteCommand;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
@Tag(name = Constants.SURVEY)
public class VoteController {

    private final CreateVoteUseCase createVoteUseCase;

    @PostMapping
    public CreateVoteResponse createVote(
        @RequestBody CreateVoteRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long createdMemberId = memberPrincipal.getMemberId();

        CreateVoteCommand cmd = request.toCommand(createdMemberId);
        Long voteId = createVoteUseCase.create(cmd);

        return new CreateVoteResponse(voteId);
    }
}
