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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger")
@RequiredArgsConstructor
@Tag(name = "Challenger | 챌린저 검색", description = "조회 중에서 검색 API만 따로 빠져 있습니다")
public class ChallengerSearchController {

    private final SearchChallengerUseCase searchChallengerUseCase;

    @Operation(summary = "챌린저 검색 (Cursor 기반)")
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

    @Operation(summary = "챌린저 검색 (Offset 기반)")
    @GetMapping("search/offset")
    SearchChallengerResponse searchChallenger(
        @ParameterObject Pageable pageable,
        @ParameterObject SearchChallengerRequest searchRequest
    ) {
        return SearchChallengerResponse.from(
            searchChallengerUseCase.search(
                searchRequest.toQuery(),
                pageable
            )
        );
    }

    @Operation(summary = "챌린저 전체 검색 (Cursor 기반, 일정 생성용)",
        description = """
            `search/cursor` 및 `search/offset` API를 사용해주세요.

            전체 챌린저를 대상으로 이름 또는 닉네임을 이용해 챌린저를 검색합니다.
            """)
    @GetMapping("search/global")
    @Deprecated(since = "2026-02-16", forRemoval = true)
    GlobalSearchChallengerResponse globalSearchChallenger(
        @ParameterObject GlobalSearchChallengerRequest searchRequest
    ) {
        return GlobalSearchChallengerResponse.from(
            searchChallengerUseCase.globalCursorSearch(
                searchRequest.toQuery(),
                searchRequest.cursor(),
                searchRequest.getSize()
            )
        );
    }
}
