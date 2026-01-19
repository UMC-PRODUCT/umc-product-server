package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumProgressResponse;
import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumProgressUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class CurriculumController implements CurriculumControllerApi {

    private final GetCurriculumProgressUseCase getCurriculumProgressUseCase;

    public CurriculumProgressResponse getMyProgress(
            @PathVariable Long challengerId
    ) {
        // TODO: 인증 개선 필요
        CurriculumProgressInfo info = getCurriculumProgressUseCase.getMyProgress(challengerId);
        return CurriculumProgressResponse.from(info);
    }
}
