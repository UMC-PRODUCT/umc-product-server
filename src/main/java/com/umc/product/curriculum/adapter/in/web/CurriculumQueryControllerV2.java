package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumProgressResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumResponse;
import com.umc.product.curriculum.adapter.in.web.swagger.CurriculumQueryControllerV2Api;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumProgressUseCase;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/curriculums")
@RequiredArgsConstructor
public class CurriculumQueryControllerV2 implements CurriculumQueryControllerV2Api {

    private final GetCurriculumUseCase getCurriculumUseCase;
    private final GetCurriculumProgressUseCase getCurriculumProgressUseCase;

    @Public
    @Override
    @GetMapping("/{gisuId}")
    public CurriculumResponse getCurriculum(
        @PathVariable Long gisuId,
        @RequestParam ChallengerPart part,
        @RequestParam(required = false) Integer week
    ) {
        return CurriculumResponse.from(getCurriculumUseCase.getByGisuAndPart(gisuId, part, week));
    }

    @Override
    @GetMapping("/{gisuId}/challengers/me/progress")
    public CurriculumProgressResponse getMyProgress(
        @PathVariable Long gisuId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return CurriculumProgressResponse.from(
            getCurriculumProgressUseCase.getMyProgressByGisu(memberPrincipal.getMemberId(), gisuId)
        );
    }
}
