package com.umc.product.challenger.adapter.in.web.v2;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.challenger.adapter.in.web.v2.dto.response.ChallengerSearchV2Response;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.adapter.in.web.dto.request.SearchMemberRequest;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * /api/v2/challenger/admin 의 관리자 Query 엔드포인트.
 * <p>
 * 기존 챌린저 검색 v2와 같은 검색 조건과 응답 계약을 사용하되, 조회 대상 기수의 운영진 역할과 소속 조직에 따라
 * 검색 가능한 지부·학교 범위를 제한합니다.
 * <p>
 * 권한 정책: SUPER_ADMIN은 전체 범위, 중앙 운영진은 역할을 보유한 기수 전체, 지부장은 본인 지부,
 * 학교 운영진은 본인 학교만 검색할 수 있습니다. 일반 챌린저 계정이나 해당 기수에 허용된 운영진 역할이
 * 없는 계정은 검색할 수 없습니다.
 */
@RestController
@RequestMapping("/api/v2/challenger/admin")
@RequiredArgsConstructor
@Tag(name = "Challenger V2 | 챌린저 Query", description = "챌린저 단위 검색 결과를 제공합니다.")
public class ChallengerAdminSearchV2Controller {

    private final SearchMemberUseCase searchMemberUseCase;

    @Operation(
        operationId = "CHALLENGER-202",
        summary = "관리자용 챌린저 검색 v2",
        description = """
            운영진 권한과 소속 범위를 적용하는 챌린저 단위 페이지네이션 검색입니다.

            - 기존 `/api/v2/challenger/search`와 같은 검색 조건과 응답 형식을 사용합니다.
            - 같은 회원이 여러 기수에 참여했다면 기수별 챌린저가 각각 별도 row로 반환됩니다.
            - `gisuId`를 생략하면 활성 기수를 기준으로 검색합니다.
            - `SUPER_ADMIN`은 모든 기수·지부·학교를 검색할 수 있습니다.
            - 중앙 운영진은 역할을 보유한 기수 전체를 검색할 수 있습니다.
            - 지부장은 역할을 보유한 기수의 본인 지부만 검색할 수 있습니다.
            - 학교 운영진은 역할을 보유한 기수의 본인 지부·학교만 검색할 수 있습니다.
            - 일반 챌린저 계정, 해당 기수에 허용된 운영진 역할이 없는 계정 또는 허용 범위 밖 필터를 지정한 계정에는
              `403 / MEMBER-0015`를 반환합니다.
            - 검색 결과의 이메일은 마스킹하며, 기본 페이지 크기는 10입니다.
            """
    )
    @GetMapping("search")
    public ChallengerSearchV2Response searchChallengersForAdmin(
        @ParameterObject @PageableDefault(size = 10) Pageable pageable,
        @ParameterObject SearchMemberRequest searchRequest,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        return ChallengerSearchV2Response.from(
            searchMemberUseCase.searchChallengersByV2WithScope(
                searchRequest.toQuery(),
                memberPrincipal.getMemberId(),
                pageable
            )
        ).withMaskedEmails();
    }
}
