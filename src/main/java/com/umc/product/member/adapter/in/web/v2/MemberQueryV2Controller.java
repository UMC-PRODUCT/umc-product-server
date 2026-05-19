package com.umc.product.member.adapter.in.web.v2;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.adapter.in.web.dto.request.SearchMemberRequest;
import com.umc.product.member.adapter.in.web.v2.dto.response.MemberSummaryV2Response;
import com.umc.product.member.adapter.in.web.v2.dto.response.SearchMemberV2Response;
import com.umc.product.member.application.port.in.query.GetMemberSummaryV2UseCase;
import com.umc.product.member.application.port.in.query.SearchMemberUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/v2/member 의 Query 엔드포인트.
 * <p>
 * BFF 패턴으로, 프로필/활성 기수 멤버십/활동일/이력/상벌점을 한 호출에 제공합니다.
 * <p>
 * 권한 정책:
 * <p>
 * - /me : 인증된 사용자 본인 정보 (인증 필터로 보호, @CheckAccess 불필요)
 * <p>
 * - /search : v1과 동일 정책. 별도 권한 어노테이션 없음 (v1 회귀 방지). 권한 강화는 별도 PR.
 */
@RestController
@RequestMapping("/api/v2/member")
@RequiredArgsConstructor
@Tag(name = "Member V2 | 회원 Query", description = "BFF 패턴이 적용되었습니다.")
public class MemberQueryV2Controller {

    private final GetMemberSummaryV2UseCase getMemberSummaryV2UseCase;
    private final SearchMemberUseCase searchMemberUseCase;

    @Operation(
        summary = "[MEMBER-201] 내 종합 정보 조회",
        description = """
            현재 로그인한 사용자의 통합 정보를 반환합니다.

            - 기본 프로필 및 소셜 링크
            - 모든 참여 기수의 활동 기간을 합산한 `totalActivityDays`
            - `currentGisuMembership`
              - 활성 기수 정보 (gisuId, generation)
              - 활성 기수에 ACTIVE 상태인 챌린저 신분 (없으면 null)
              - 활성 기수에 운영진 ChallengerRole 하나라도 보유 여부 (`isAdmin`)
              - 보유 운영진 RoleType 목록
              - 휴지기에는 `currentGisuMembership = null`
            - `challengerHistory` : 모든 기수의 챌린저 이력 (최신 기수 우선). 기수별 상벌점 포함.
            """
    )
    @GetMapping("me")
    public MemberSummaryV2Response getMySummary(@CurrentMember MemberPrincipal memberPrincipal) {
        return MemberSummaryV2Response.from(
            getMemberSummaryV2UseCase.getSummaryByMemberId(memberPrincipal.getMemberId())
        );
    }

    @Operation(
        summary = "[MEMBER-202] 회원 검색 v2",
        description = """
            v1 검색 응답에 추가로 다음 두 필드를 제공합니다.

            - `challengerStatus` : 해당 행 챌린저의 상태 (ACTIVE/GRADUATED/EXPELLED/WITHDRAWN)
            - `isAdminInActiveGisu` : 회원이 현재 활성 기수에 운영진 ChallengerRole을 하나라도 보유하는지

            검색 조건/필터는 v1과 동일합니다.

            검색 결과에는 본인 외 회원이 포함되므로, 로그인 식별자인 이메일은 평문 노출을 피하기 위해
            컨트롤러 단에서 마스킹 처리되어 응답됩니다.
            """
    )
    @GetMapping("search")
    public SearchMemberV2Response searchMembersV2(
        @ParameterObject Pageable pageable,
        @ParameterObject SearchMemberRequest searchRequest
    ) {
        return SearchMemberV2Response.from(
            searchMemberUseCase.searchByV2(searchRequest.toQuery(), pageable)
        ).withMaskedEmails();
    }
}
