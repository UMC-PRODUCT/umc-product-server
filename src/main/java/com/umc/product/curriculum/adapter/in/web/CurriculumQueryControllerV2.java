package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumProgressResponse;
import com.umc.product.curriculum.adapter.in.web.swagger.CurriculumQueryControllerV2Api;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumProgressUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProgressInfo;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/curriculums")
@RequiredArgsConstructor
public class CurriculumQueryControllerV2 implements CurriculumQueryControllerV2Api {

    private final GetCurriculumProgressUseCase getCurriculumProgressUseCase;

    @Override
    @GetMapping("/challengers/me/progress")
    public CurriculumProgressResponse getMyProgress(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId
    ) {
        CurriculumProgressInfo info = getCurriculumProgressUseCase.getMyProgressByGisu(
            memberPrincipal.getMemberId(), gisuId
        );
        return CurriculumProgressResponse.from(info);
    }
}
