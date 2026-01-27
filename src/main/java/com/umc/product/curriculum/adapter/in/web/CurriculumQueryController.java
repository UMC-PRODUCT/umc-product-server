package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumProgressResponse;
import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumProgressUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class CurriculumQueryController implements CurriculumQueryControllerApi {

    private final GetCurriculumProgressUseCase getCurriculumProgressUseCase;

    @GetMapping("/challengers/me/progress")
    public CurriculumProgressResponse getMyProgress(
    ) {
        // TODO: memberPrincipal로 바꾸기
        Long challengerId = 1L;

        CurriculumProgressInfo info = getCurriculumProgressUseCase.getMyProgress(challengerId);
        return CurriculumProgressResponse.from(info);
    }
}
