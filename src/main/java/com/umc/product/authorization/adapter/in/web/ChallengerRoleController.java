package com.umc.product.authorization.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.adapter.in.web.dto.request.CreateChallengerRoleRequest;
import com.umc.product.authorization.adapter.in.web.dto.response.ChallengerRoleResponse;
import com.umc.product.authorization.adapter.in.web.dto.response.CreateChallengerRoleResponse;
import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.command.dto.DeleteChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authorization | 운영진 권한 관리", description = "직책 부여, 수정, 삭제 등")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/authorization/challenger-role")
public class ChallengerRoleController {

    private final ManageChallengerRoleUseCase manageChallengerRoleUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    private final GetGisuUseCase getGisuUseCase;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_ROLE,
        permission = PermissionType.WRITE
    )
    @PostMapping
    @Operation(summary = "운영진 기록 생성", description = "ChallengerRole, 즉 운영진 기록을 생성합니다. 총괄단만 가능합니다.")
    public CreateChallengerRoleResponse createChallengerRole(
        @RequestBody CreateChallengerRoleRequest request) {
        Long createdId =
            manageChallengerRoleUseCase.createChallengerRole(CreateChallengerRoleCommand.from(request));

        return CreateChallengerRoleResponse.builder()
            .challengerRoleId(createdId)
            .build();
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_ROLE,
        permission = PermissionType.READ
    )
    @Deprecated(since = "v2.0.0", forRemoval = true)
    @Operation(summary = "운영진 기록 조회", description = "deprecate: 내 프로필 조회 등에서 확인할 수 있는 정보인 관계로 중복 API를 제거합니다.")
    @GetMapping("{challengerRoleId}")
    public ChallengerRoleResponse getChallengerRole(
        @PathVariable Long challengerRoleId
    ) {
        ChallengerRoleInfo challengerRoleInfo = getChallengerRoleUseCase.getById(challengerRoleId);

        return ChallengerRoleResponse.from(
            challengerRoleInfo, getGisuInfo(challengerRoleInfo.gisuId())
        );
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER_ROLE,
        permission = PermissionType.DELETE
    )
    @Operation(summary = "운영진 기록 삭제", description = "부여된 운영진 권한(기록)을 삭제합니다.")
    @DeleteMapping("{challengerRoleId}")
    public void deleteChallengerRole(
        @PathVariable Long challengerRoleId
    ) {
        manageChallengerRoleUseCase.deleteChallengerRole(
            DeleteChallengerRoleCommand.builder()
                .challengerRoleId(challengerRoleId)
                .build()
        );
    }


    private GisuInfo getGisuInfo(Long gisuId) {
        return getGisuUseCase.getById(gisuId);
    }
}
