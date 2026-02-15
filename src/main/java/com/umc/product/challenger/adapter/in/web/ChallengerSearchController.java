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

    @Deprecated
    @Operation(summary = "챌린저 검색", deprecated = true,
        description = "Deprecated: cursor와 offset을 분리하기 위해 엔드포인트를 변경합니다. 해당 API는 사용하지 않습니다.")
    @GetMapping("search")
    SearchChallengerResponse searchChallenger() {
        throw new UnsupportedOperationException("이 API는 더 이상 지원되지 않습니다. cursor와 offset 기반 검색을 위한 별도의 엔드포인트를 사용하세요.");
    }

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
        description = "전체 챌린저를 대상으로 이름 또는 닉네임을 이용해 챌린저를 검색합니다.")
    @GetMapping("search/global")
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
