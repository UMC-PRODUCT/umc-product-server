package com.umc.product.challenger.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.challenger.adapter.in.web.dto.request.AddChallengerDeactivationEvidenceRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.RegisterChallengerDeactivationRequest;
import com.umc.product.challenger.adapter.in.web.dto.response.RegisterChallengerDeactivationResponse;
import com.umc.product.challenger.application.port.in.command.AddChallengerDeactivationEvidenceUseCase;
import com.umc.product.challenger.application.port.in.command.RegisterChallengerDeactivationUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/challenger/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Challenger | 탈부 관리 Command", description = "관리자용 챌린저 탈부 등록 기능을 제공합니다.")
public class ChallengerDeactivationCommandController {

    private final RegisterChallengerDeactivationUseCase registerChallengerDeactivationUseCase;
    private final AddChallengerDeactivationEvidenceUseCase addChallengerDeactivationEvidenceUseCase;

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        resourceId = "#challengerId",
        permission = PermissionType.MANAGE,
        message = "해당 챌린저의 탈부를 등록할 권한이 없어요. 대상 기수와 소속 범위를 확인해주세요."
    )
    @PostMapping("/{challengerId}/deactivations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "CHALLENGER-006",
        summary = "챌린저 탈부 등록",
        description = """
            탈부 정보를 등록하고 챌린저 상태를 변경합니다.

            - 근거 자료와 메모는 선택 사항입니다.
            - reason이 VOLUNTARY_OTHER 또는 INVOLUNTARY_OTHER인 경우 reasonDetail은 필수입니다.

            이미지를 등록하는 경우, 아래 절차로 Storage API를 통한 이미지 업로드를 완료한 후 탈부 등록 API를 호출해야 합니다.

            1. `POST /api/v1/storage/prepare-upload` 호출
                - 요청의 `category`에 `CHALLENGER_DEACTIVATION_EVIDENCE` 전달
            2. 응답의 `uploadUrl`, `uploadMethod`, `headers`를 사용하여 발급된 업로드 주소로 파일 전송
            3. 파일 전송 성공 후 `POST /api/v1/storage/{fileId}/confirm` 호출
            4. 업로드가 완료된 `fileId`를 이 API 요청의 `imageIds`에 전달
            """
    )
    public RegisterChallengerDeactivationResponse register(
        @PathVariable Long challengerId,
        @Valid @RequestBody RegisterChallengerDeactivationRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        Long deactivationId = registerChallengerDeactivationUseCase.register(
            request.toCommand(challengerId, principal.getMemberId())
        );
        return RegisterChallengerDeactivationResponse.from(deactivationId);
    }

    @CheckAccess(
        resourceType = ResourceType.CHALLENGER,
        resourceId = "#challengerId",
        permission = PermissionType.MANAGE,
        message = "해당 챌린저의 탈부 근거 자료를 추가할 권한이 없어요. 대상 기수와 소속 범위를 확인해주세요."
    )
    @PostMapping("/{challengerId}/deactivations/evidence")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "CHALLENGER-007",
        summary = "챌린저 탈부 근거 자료 추가",
        description = """
            기존 탈부 기록에 근거 사진 또는 링크를 추가하고 진행 기록을 저장합니다.
            사진과 링크 중 하나 이상을 전달해야 합니다.

            이미지를 추가하는 경우, 아래 절차로 Storage API를 통한 이미지 업로드를 완료한 후 근거 자료 추가 API를 호출해야 합니다.

            1. `POST /api/v1/storage/prepare-upload` 호출
                - 요청의 `category`에 `CHALLENGER_DEACTIVATION_EVIDENCE` 전달
            2. 응답의 `uploadUrl`, `uploadMethod`, `headers`를 사용하여 발급된 업로드 주소로 파일 전송
            3. 파일 전송 성공 후 `POST /api/v1/storage/{fileId}/confirm` 호출
            4. 업로드가 완료된 `fileId`를 이 API 요청의 `imageIds`에 전달
            """
    )
    public void addEvidence(
        @PathVariable Long challengerId,
        @Valid @RequestBody AddChallengerDeactivationEvidenceRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        addChallengerDeactivationEvidenceUseCase.addEvidence(
            request.toCommand(challengerId, principal.getMemberId())
        );
    }
}
