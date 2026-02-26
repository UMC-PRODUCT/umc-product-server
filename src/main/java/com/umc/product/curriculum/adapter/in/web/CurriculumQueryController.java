package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumProgressResponse;
import com.umc.product.curriculum.adapter.in.web.dto.response.CurriculumWeeksResponse;
import com.umc.product.curriculum.adapter.in.web.swagger.CurriculumQueryControllerApi;
import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.in.query.CurriculumWeekInfo;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumProgressUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class CurriculumQueryController implements CurriculumQueryControllerApi {

    private final GetCurriculumProgressUseCase getCurriculumProgressUseCase;

    @Override
    @GetMapping("/challengers/me/progress")
    public CurriculumProgressResponse getMyProgress(@CurrentMember MemberPrincipal memberPrincipal) {
        CurriculumProgressInfo info = getCurriculumProgressUseCase.getMyProgress(memberPrincipal.getMemberId());
        return CurriculumProgressResponse.from(info);
    }

    @Override
    @GetMapping("/weeks")
    public CurriculumWeeksResponse getWeeksByPart(@RequestParam ChallengerPart part) {
        List<CurriculumWeekInfo> weeks = getCurriculumProgressUseCase.getWeeksByPart(part);
        return CurriculumWeeksResponse.from(weeks);
    }
}
