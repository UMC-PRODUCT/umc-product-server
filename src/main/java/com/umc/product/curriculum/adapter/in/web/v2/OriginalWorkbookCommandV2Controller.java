package com.umc.product.curriculum.adapter.in.web.v2;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.curriculum.adapter.in.web.v2.dto.request.ChangeOriginalWorkbookStatusRequest;
import com.umc.product.curriculum.adapter.in.web.v2.dto.request.CreateOriginalWorkbookRequest;
import com.umc.product.curriculum.adapter.in.web.v2.dto.request.EditOriginalWorkbookRequest;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.ChangeOriginalWorkbookStatusCommand;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/curriculums/original-workbooks")
@RequiredArgsConstructor
@Tag(name = "Curriculum V2 | Original Workbook Command", description = "중앙운영사무국 교육국 소속 파트장용. 주차별 원본 워크북 생성/수정/삭제 등")
public class OriginalWorkbookCommandV2Controller {

    private final ManageOriginalWorkbookUseCase manageOriginalWorkbookUseCase;

    @Operation(
        summary = "[ORIGINAL-WORKBOOK-001] 중앙파트장용: 원본 워크북 추가 (READY 상태)",
        description = """
            주차별 커리큘럼에 원본 워크북을 추가합니다. 생성 즉시 **배포 준비(READY)** 상태가 됩니다.

            READY 상태의 워크북은 배포 시점(주차 시작 2주 전)에 스케줄러에 의해 자동 배포될 수 있습니다.
            임시저장이 필요한 경우 `/draft` 엔드포인트를 사용하세요.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.ORIGINAL_WORKBOOK,
        permission = PermissionType.MANAGE,
        message = "원본 워크북을 만들려면 중앙 파트장 이상 권한이 필요해요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @PostMapping
    public Long createOriginalWorkbook(
        @Valid @RequestBody CreateOriginalWorkbookRequest request
    ) {
        return manageOriginalWorkbookUseCase.create(request.toCommand(OriginalWorkbookStatus.READY));
    }

    @Operation(
        summary = "[ORIGINAL-WORKBOOK-002] 중앙파트장용: 원본 워크북 임시저장 (DRAFT 상태)",
        description = """
            주차별 커리큘럼에 원본 워크북을 **임시저장(DRAFT)** 상태로 추가합니다.

            DRAFT 상태의 워크북은 스케줄러 자동 배포 대상에서 제외됩니다.
            배포 준비가 완료되면 상태 변경 API로 READY 상태로 전환하세요.

            상태 전환 흐름: `DRAFT` → `READY` → `RELEASED`
            """
    )
    @CheckAccess(
        resourceType = ResourceType.ORIGINAL_WORKBOOK,
        permission = PermissionType.MANAGE,
        message = "원본 워크북을 임시저장하려면 중앙 파트장 이상 권한이 필요해요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @PostMapping("/draft")
    public Long createOriginalWorkbookAsDraft(
        @Valid @RequestBody CreateOriginalWorkbookRequest request
    ) {
        return manageOriginalWorkbookUseCase.create(request.toCommand(OriginalWorkbookStatus.DRAFT));
    }

    @Operation(
        summary = "[ORIGINAL-WORKBOOK-003] 중앙파트장용: 원본 워크북 수정",
        description = """
            원본 워크북의 제목 및 내용 등을 수정할 수 있습니다.
            따로 제한 없이 수정이 가능하며, 수정에 따른 책임은 중앙 파트장에게 있습니다.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.ORIGINAL_WORKBOOK,
        resourceId = "#originalWorkbookId",
        permission = PermissionType.MANAGE,
        message = "원본 워크북을 수정하려면 중앙 파트장 이상 권한이 필요해요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @PatchMapping("/{originalWorkbookId}")
    public void editOriginalWorkbook(
        @PathVariable Long originalWorkbookId,
        @RequestBody EditOriginalWorkbookRequest request
    ) {
        manageOriginalWorkbookUseCase.edit(request.toCommand(originalWorkbookId));
    }

    @Operation(
        summary = "[ORIGINAL-WORKBOOK-004] 중앙파트장용: 원본 워크북 삭제",
        description = """
            배포받은 사용자가 존재하는 경우에는 삭제가 불가능합니다.
            """
    )
    @CheckAccess(
        resourceType = ResourceType.ORIGINAL_WORKBOOK,
        resourceId = "#originalWorkbookId",
        permission = PermissionType.MANAGE,
        message = "원본 워크북을 삭제하려면 중앙 파트장 이상 권한이 필요해요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @DeleteMapping("/{originalWorkbookId}")
    public void deleteOriginalWorkbook(
        @PathVariable Long originalWorkbookId
    ) {
        manageOriginalWorkbookUseCase.delete(originalWorkbookId);
    }

    @Operation(
        summary = "[ORIGINAL-WORKBOOK-005] 중앙파트장용: 원본 워크북 상태 일괄 변경",
        description = """
            여러 원본 워크북의 상태를 한 번에 변경합니다.
            요청 중 하나라도 실패하면 **모든 요청이 함께 롤백**됩니다.

            #### 허용된 상태 전환
            | 현재 상태 | 목표 상태 | 허용 여부 |
            |---|---|---|
            | DRAFT | READY | ✅ (배포 준비 등록) |
            | READY | RELEASED | ✅ (수동 배포) |
            | READY | DRAFT | ✅ (임시저장으로 롤백) |
            | RELEASED | any | ❌ (배포 후 되돌리기 불가) |
            | DRAFT | RELEASED | ❌ (READY 경유 필수) |
            """
    )
    @CheckAccess(
        resourceType = ResourceType.ORIGINAL_WORKBOOK,
        permission = PermissionType.RELEASE,
        message = "원본 워크북 상태를 수정하려면 중앙 파트장 이상 권한이 필요해요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @PatchMapping("/status")
    public void changeOriginalWorkbookStatus(
        @Valid @RequestBody List<ChangeOriginalWorkbookStatusRequest> requests,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        List<ChangeOriginalWorkbookStatusCommand> commands = requests.stream()
            .map(r -> r.toCommand(memberPrincipal.getMemberId()))
            .toList();
        manageOriginalWorkbookUseCase.changeStatusForRelease(commands);
    }
}
