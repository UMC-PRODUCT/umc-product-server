package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.request.GlobalSearchChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.SearchChallengerCursorRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.SearchChallengerRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.CursorSearchChallengerResponse;
import com.umc.product.challenger.adapter.in.web.dto.response.GlobalSearchChallengerResponse;
import com.umc.product.challenger.adapter.in.web.dto.response.SearchChallengerResponse;
import com.umc.product.challenger.application.port.in.query.SearchChallengerUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Challenger | 챌린저 검색", description = "조회 중에서 검색 API만 따로 빠져 있습니다")
public class ChallengerSearchController {

    private final SearchChallengerUseCase searchChallengerUseCase;

    @Operation(summary = "챌린저 검색 (Cursor 기반)",
        description = """
             가능한 필터는 이름, 닉네임, 학교, 지부, 파트, 기수 입니다.\s\s
             이름과 닉네임은, 통합하여 keyword 파라미터로 전달할 수도 있으며 keyword가 제공된 경우 name/nickname 파라미터는 무시됩니다. (즉, keyword가 있으면 name/nickname은 검색 조건에서 제외됩니다)\s\s
             제공된 파라미터들은 모두 AND 조건으로 검색됩니다. (keyword가 제공된 경우, name/nickname 파라미터를 제외한 나머지 파라미터와 AND 연산됩니다)\s\s

             e.g. `keyword=하늘&schoolId=1` -> 이름 또는 닉네임에 "하늘"이 포함되고, 소속 학교 ID가 1인 챌린저 검색\s\s
             e.g. `keyword=하늘&name=경운&schoolId=1` -> 이름 또는 닉네임에 "하늘"이 포함되고, 소속 학교 ID가 1인 챌린저 검색 (name에 전달된 "경운"은 무시됨)\s\s
             e.g. `name=홍&nickname=길동&part=WEB` -> 이름에 "홍"이 포함되고, 닉네임에 "길동"이 포함되고, 파트가 WEB인 챌린저 검색\s\s
            """)
    @GetMapping("search/cursor")
    CursorSearchChallengerResponse cursorSearchChallenger(
        @ParameterObject SearchChallengerCursorRequest searchRequest
    ) {
        return CursorSearchChallengerResponse.from(
            searchChallengerUseCase.cursorSearch(
                searchRequest.toQuery(),
                searchRequest.cursor(),
                searchRequest.getSize()
            )
        );
    }

    @Operation(summary = "챌린저 검색 (Offset 기반)", description = "Cursor 기반 검색과 동일한 검색 조건을 활용합니다. `search/cursor`의 설명을 읽어주세요.")
    @GetMapping("search/offset")
    SearchChallengerResponse searchChallenger(
        @ParameterObject Pageable pageable,
        @ParameterObject SearchChallengerRequest searchRequest
    ) {
        return SearchChallengerResponse.from(
            searchChallengerUseCase.offsetSearch(
                searchRequest.toQuery(),
                pageable
            )
        );
    }

    @Operation(summary = "deprecated: 챌린저 전체 검색 (Cursor 기반, 일정 생성용)",
        description = """
            `search/cursor` 및 `search/offset` API를 사용해주세요.

            전체 챌린저를 대상으로 이름 또는 닉네임을 이용해 챌린저를 검색합니다.
            """)
    @GetMapping("search/global")
    @Deprecated(since = "v1.2.5", forRemoval = true)
    GlobalSearchChallengerResponse globalSearchChallenger(
        @ParameterObject GlobalSearchChallengerRequest searchRequest
    ) {
        log.warn("Deprecated API 호출: /api/v1/challenger/search/global");

        return GlobalSearchChallengerResponse.from(
            searchChallengerUseCase.globalCursorSearch(
                searchRequest.toQuery(),
                searchRequest.cursor(),
                searchRequest.getSize()
            )
        );
    }
}
