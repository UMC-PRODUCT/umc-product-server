package com.umc.product.curriculum.adapter.in.web.v1;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.v1.dto.response.CurriculumProgressResponse;
import com.umc.product.curriculum.adapter.in.web.v1.dto.response.CurriculumResponse;
import com.umc.product.curriculum.adapter.in.web.v1.dto.response.CurriculumWeeksResponse;
import com.umc.product.curriculum.adapter.in.web.v1.swagger.CurriculumQueryControllerApi;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumWeekInfo;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
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

    private final GetCurriculumUseCase getCurriculumUseCase;
    private final GetGisuUseCase getGisuUseCase;

    /**
     * @since 1.3.0
     * @deprecated {@code GET /api/v2/curriculums?gisuId={gisuId}&part={part}} 사용 권장.
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    @Public
    @Override
    @GetMapping
    public CurriculumResponse getCurriculum(
        @RequestParam ChallengerPart part
    ) {
        Long activeGisuId = getGisuUseCase.getActiveGisuId();
        return CurriculumResponse.from(getCurriculumUseCase.getByGisuAndPart(activeGisuId, part, null));
    }

    /**
     * @since 1.3.0
     * @deprecated {@code GET /api/v2/curriculums/challengers/me/progress?gisuId={gisuId}} 사용 권장.
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    @Override
    @GetMapping("/challengers/me/progress")
    public CurriculumProgressResponse getMyProgress(@CurrentMember MemberPrincipal memberPrincipal) {
        CurriculumProgressInfo info = getCurriculumUseCase.getMyProgress(memberPrincipal.getMemberId());
        return CurriculumProgressResponse.from(info);
    }

    /**
     * @since 1.3.0
     * @deprecated 대체 API 미정. 추후 v2 엔드포인트 추가 예정.
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    @Override
    @GetMapping("/weeks")
    public CurriculumWeeksResponse getWeeksByPart(@RequestParam ChallengerPart part) {
        List<CurriculumWeekInfo> weeks = getCurriculumUseCase.getWeeksByPart(part);
        return CurriculumWeeksResponse.from(weeks);
    }
}
