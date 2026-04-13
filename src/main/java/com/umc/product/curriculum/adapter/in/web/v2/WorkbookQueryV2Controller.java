package com.umc.product.curriculum.adapter.in.web.v2;

import com.umc.product.curriculum.adapter.in.web.v2.dto.request.GetBestWorkbooksRequest;
import com.umc.product.curriculum.adapter.in.web.v2.dto.response.BestWorkbookResponse;
import com.umc.product.curriculum.adapter.in.web.v2.dto.response.ChallengerWorkbookResponse;
import com.umc.product.curriculum.adapter.in.web.v2.dto.response.OriginalWorkbookResponse;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/curriculums")
@RequiredArgsConstructor
@Tag(name = "Curriculum V2 | 워크북 Query", description = "OriginalWorkbook, ChallengerWorkbook, BestWorkbook 조회")
public class WorkbookQueryV2Controller {
    @Operation(
        summary = "OriginalWorkbook 상세 조회",
        description = """
            원본 워크북을 조회합니다. 원본 워크북의 파트와 기수에 해당 파트의 스터디 그룹에 속해 있어야 합니다.
            """
    )
    @GetMapping("/original-workbooks/{originalWorkbookId}")
    public OriginalWorkbookResponse getOriginalWorkbook(
        @PathVariable String originalWorkbookId
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "ChallengerWorkbook 상세 조회",
        description = """
            챌린저 워크북과 그에 연관된 미션 제출물 및 피드백을 조회합니다.

            꼭 본인이 아니더라도, 같은 기수에 활동한 챌린저 전원은 확인할 수 있습니다.
            기획단 결정사항에 따라서 미션 제출 내역 및 피드백 내용까지 볼 수 있습니다.
            """
    )
    @GetMapping("/challenger-workbooks/{challengerWorkbookId}")
    public ChallengerWorkbookResponse getChallengerWorkbook(
        @PathVariable String challengerWorkbookId
    ) {
        throw new NotImplementedException();
    }

    @Operation(
        summary = "베스트 워크북 조회",
        description = """
            베스트 워크북을 조회합니다. Cursor Pagination이 적용되었습니다.

            Query Param으로 아래와 같은 필터를 적용할 수 있으며,
            다중 선택을 지원하며, 제공된 값들에 대한 카르테시안 곱으로 결과를 제공합니다.

            - 기수 ID
            - (다중 선택 가능) 학교 ID
            - (다중 선택 가능) 파트 (제공되지 않은 경우 전체 파트)
            - (다중 선택 가능) 주차
            - (다중 선택 가능) 스터디 그룹 ID
            """
    )
    @GetMapping("/weekly-best-workbooks")
    public PageResponse<BestWorkbookResponse> getBestWorkbooks(
        @ParameterObject
        @RequestParam(required = false)
        GetBestWorkbooksRequest request
    ) {
        // TODO: 경운 - 이거 IN Query로 안짜면 죽어도 Approve 안해줄거임,,,,,,

        throw new NotImplementedException();
    }
}
