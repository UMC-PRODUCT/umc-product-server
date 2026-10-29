package com.umc.product.project.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.SaveProjectQuotaManagementRequest;
import com.umc.product.project.adapter.in.web.dto.response.SaveProjectQuotaManagementResponse;
import com.umc.product.project.application.port.in.command.SaveProjectQuotaManagementUseCase;
import com.umc.product.project.application.port.in.query.GetProjectQuotaManagementUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects/admin/part-quotas")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 TO 관리")
public class ProjectQuotaManagementController {
    private final GetProjectQuotaManagementUseCase getProjectQuotaManagementUseCase;
    private final SaveProjectQuotaManagementUseCase saveProjectQuotaManagementUseCase;

    @GetMapping
    @Operation(operationId = "PROJECT-QUOTAS-001", summary = "프로젝트 TO 관리 화면 조회", description = """
        최고관리자·소속 기수 총괄단·소속 지부장만 접근할 수 있습니다. chapterId 생략 시 접근 가능한 첫 지부를 선택합니다.
        지부·학교 선택지, 파트별 인원·TO 합계, 필터 결과와 전체 지부 합계, 프로젝트별 셀을 한 번에 반환합니다.
        columns.canEdit와 cells.canEdit에는 파트별 수정 가능 여부·거부 사유가 있으며 editableAfter는 3차 PM 결정 마감입니다.
        projects는 프로젝트명·ID순 전체 목록으로 내부 스크롤 표에 사용합니다. 숫자는 공통 JSON 규칙대로 문자열입니다.
        """)
    public ProjectQuotaManagementInfo get(
        @CurrentMember MemberPrincipal principal,
        @RequestParam Long gisuId,
        @RequestParam(required = false) Long chapterId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long schoolId
    ) {
        return getProjectQuotaManagementUseCase.get(principal.getMemberId(), gisuId, chapterId, keyword, schoolId);
    }

    @PatchMapping
    @Operation(operationId = "PROJECT-QUOTAS-002", summary = "변경한 프로젝트 TO 셀 일괄 저장", description = """
        변경한 프로젝트·셀만 전달합니다. 누락된 셀·프로젝트는 유지하며 quota=0은 해당 파트의 모집 TO를 제거합니다.
        Web PE와 Mobile PE는 동시에 모집할 수 없습니다(PROJECT-0255). 기존 파트 TO 0과 새 파트 TO를 함께 전달해 전환합니다.
        PM TO도 수정할 수 있으나 실제 PM·공동 PM 인원 미만으로 줄일 수 없습니다.
        파트별 1차 시작~3차 PM 결정 마감에는 변경을 차단합니다. PM TO는 두 매칭 중 하나라도 잠겨 있으면 차단합니다.
        발표 대기를 포함한 기존 배정 인원 미만 축소와 지부 파트별 인원 초과를 거부합니다. 부족은 허용합니다.
        하나라도 실패하면 전체 저장을 롤백합니다. 저장 후 같은 GET을 재조회해 합계·권한을 갱신합니다.
        초기화는 프런트의 미저장 입력을 되돌리는 동작이며 별도 삭제 API를 호출하지 않습니다.
        """)
    public SaveProjectQuotaManagementResponse save(
        @CurrentMember MemberPrincipal principal,
        @Valid @RequestBody SaveProjectQuotaManagementRequest request
    ) {
        return SaveProjectQuotaManagementResponse.from(
            saveProjectQuotaManagementUseCase.save(request.toCommand(principal.getMemberId())));
    }

}
