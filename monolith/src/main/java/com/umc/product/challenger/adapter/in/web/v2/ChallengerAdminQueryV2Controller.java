package com.umc.product.challenger.adapter.in.web.v2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.challenger.adapter.in.web.assembler.ChallengerResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 관리자용 Challenger 조회 관련 기능을 담당하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v2/challenger/admin")
@RequiredArgsConstructor
@Tag(name = "Challenger V2 | 챌린저 Query", description = "관리자용 챌린저 조회 결과를 제공합니다.")
public class ChallengerAdminQueryV2Controller {

    private final ChallengerResponseAssembler assembler;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        resourceId = "#challengerId",
        permission = PermissionType.READ,
        message = "해당 챌린저의 상세 정보를 조회할 권한이 없어요. 대상 기수와 소속 범위를 확인해주세요."
    )
    @Operation(
        operationId = "CHALLENGER-203",
        summary = "관리자용 챌린저 상세 조회 v2",
        description = """
            대상 챌린저의 기수와 소속을 기준으로 로그인 사용자의 조회 권한을 검증한 뒤,
            챌린저 기본 정보와 상벌점 이력을 반환합니다.

            - 상벌점 이력은 생성 일시 기준 최신순으로 반환됩니다.
            - `SUPER_ADMIN`은 모든 챌린저를 조회할 수 있습니다.
            - 중앙 운영진은 역할을 보유한 기수의 챌린저를 조회할 수 있습니다.
            - 지부장은 역할을 보유한 기수의 본인 지부 챌린저를 조회할 수 있습니다.
            - 학교 운영진은 역할을 보유한 기수의 본인 학교 챌린저를 조회할 수 있습니다.
            - 일반 계정, 다른 기수 또는 소속 범위 밖의 챌린저를 조회하면 403을 반환합니다.
            """
    )
    @GetMapping("{challengerId}")
    public ChallengerInfoResponse getChallengerInfoForAdmin(@PathVariable Long challengerId) {
        return assembler.fromChallengerIdWithPointsLatestFirst(challengerId);
    }
}
