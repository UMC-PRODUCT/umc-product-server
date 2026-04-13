package com.umc.product.curriculum.adapter.in.web.v2;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.v2.dto.response.CurriculumOverviewResponse;
import com.umc.product.curriculum.adapter.in.web.v2.dto.response.MyCurriculumResponse;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/curriculums")
@RequiredArgsConstructor
@Tag(name = "Curriculum V2 | 커리큘럼 Query", description = "커리큘럼 및 내 진행상황 등 조회")
public class CurriculumQueryV2Controller {

    @Operation(
        summary = "특정 기수의 파트별 커리큘럼 조회",
        description = """
            UMC WEB에서 랜딩페이지 용으로 사용하는 API입니다.

            주어진 기수, 파트에 해당하는 커리큘럼에 대한 정보를 반환하며, 세부 내용은 아래와 같습니다.
            - 상위 단위, Curriculum의 제목
            - 주차별 커리큘럼, WeeklyCurriculum의 제목, N주차, 부록 여부, 시작/종료일

            `week` 파라미터를 지정하면 해당 주차의 워크북만 반환합니다.
            """
    )
    @Public
    @GetMapping("/overview")
    public CurriculumOverviewResponse getCurriculum(
        @RequestParam Long gisuId,
        @RequestParam ChallengerPart part,
        @RequestParam(required = false) Long weekNo
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "내 커리큘럼 진행 상황 조회",
        description = """
            해당 기수에 사용자가 속한 스터디 그룹에 따라 커리큘럼을 반환합니다.
            커리큘럼에 따라서 각 사용자의 워크북이 존재하는 경우에는 포함됩니다.

            1. 사용자가 속한 모든 StudyGroup 조회
            2. StudyGroup 파트에 해당하는 Curriculum 조회
            3. Curriculum - OriginalWorkbook - OriginalWorkbookMission 조회
            4. OriginalWorkbook에 대한 ChallengerWorkbook 조회
            5. OriginalWorkbookMission에 대한 MissionSubmission 조회
            6. MissionSubmission에 대한 MissionFeedback 조회
            7. 위 결과를 바탕으로 DTO 조립하기 (WeeklyCurriculum별 status 또한 평가해서 제공하기)

            지정한 기수에서 본인의 커리큘럼 진행 상황을 조회합니다.
            각 주차별 워크북의 상태(기본/진행중/제출완료/통과/실패)를 반환합니다.
            """
    )
    @GetMapping("/progress/me")
    public MyCurriculumResponse getMyProgress(
        @RequestParam Long gisuId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        throw new NotImplementedException();
    }
}
