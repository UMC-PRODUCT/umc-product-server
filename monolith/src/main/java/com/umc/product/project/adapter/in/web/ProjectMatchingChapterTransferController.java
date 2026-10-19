package com.umc.product.project.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.dto.request.ReleaseMatchingChapterTransferRequest;
import com.umc.product.project.adapter.in.web.dto.request.TransferMatchingChapterRequest;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMatchingAffiliationResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMatchingChapterTransferResponse;
import com.umc.product.project.application.port.in.command.ReleaseMatchingChapterTransferUseCase;
import com.umc.product.project.application.port.in.command.TransferMatchingChapterUseCase;
import com.umc.product.project.application.port.in.query.GetMyMatchingAffiliationUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 매칭 지부 이동", description = "프로젝트 매칭에서 챌린저가 소속될 지부를 이동하고 조회합니다.")
public class ProjectMatchingChapterTransferController {

    private final TransferMatchingChapterUseCase transferMatchingChapterUseCase;
    private final ReleaseMatchingChapterTransferUseCase releaseMatchingChapterTransferUseCase;
    private final GetMyMatchingAffiliationUseCase getMyMatchingAffiliationUseCase;

    @PostMapping("/admin/matching-chapter-transfers")
    @Operation(
        operationId = "PROJECT-TRANSFER-101",
        summary = "매칭 지부 이동 일괄 등록",
        description = """
            챌린저들의 프로젝트 매칭 지부를 다른 지부로 이동합니다. 매칭(지원·차수·통계·랜덤매칭)에만 적용되며 공지·출석 등은 원지부를 유지합니다.
            - 슈퍼어드민, 해당 기수 총괄단, 원지부 지부장, 이동지부 지부장이 등록할 수 있습니다.
            - 기획 파트와 파트가 없는 챌린저는 이동할 수 없습니다.
            - 이미 팀원이거나 합격한 지원서가 있으면 이동할 수 없습니다.
            - 임시저장·제출 상태의 지원서와, 아직 자동 선발이 실행되지 않은 차수의 불합격 지원서는 자동으로 철회됩니다.
            - 한 명이라도 실패하면 전체가 롤백되며, 실패한 회원은 에러 응답의 상세 메시지로 알려줍니다.
            """
    )
    public List<ProjectMatchingChapterTransferResponse> transfer(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Valid @RequestBody TransferMatchingChapterRequest request
    ) {
        return transferMatchingChapterUseCase.transfer(request.toCommand(memberPrincipal.getMemberId())).stream()
            .map(ProjectMatchingChapterTransferResponse::from)
            .toList();
    }

    @PostMapping("/admin/matching-chapter-transfers/{transferId}/release")
    @Operation(
        operationId = "PROJECT-TRANSFER-102",
        summary = "매칭 지부 이동 해제",
        description = """
            매칭 지부 이동을 해제해 원지부 매칭으로 되돌립니다.
            - 권한과 전제 조건은 등록과 같습니다.
            - 이동지부에서 작성·제출한 지원서와, 아직 자동 선발이 실행되지 않은 차수의 불합격 지원서는 자동으로 철회됩니다.
            """
    )
    public ProjectMatchingChapterTransferResponse release(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long transferId,
        @Valid @RequestBody(required = false) ReleaseMatchingChapterTransferRequest request
    ) {
        ReleaseMatchingChapterTransferRequest body =
            request != null ? request : new ReleaseMatchingChapterTransferRequest(null);
        return ProjectMatchingChapterTransferResponse.from(
            releaseMatchingChapterTransferUseCase.release(body.toCommand(memberPrincipal.getMemberId(), transferId))
        );
    }

    @GetMapping("/matching-affiliation/me")
    @Operation(
        operationId = "PROJECT-TRANSFER-001",
        summary = "내 매칭 소속 조회",
        description = """
            기수 내 본인의 원지부와 프로젝트 매칭 지부를 조회합니다.
            - 매칭 화면에서는 matchingChapter.id 를 매칭 차수 조회와 프로젝트 목록 필터의 chapterId 로 사용합니다.
            - gisuId 를 생략하면 활성 기수 기준입니다.
            - 학교가 지부에 배정되지 않았으면 originChapter, matchingChapter 가 null 일 수 있습니다.
            """
    )
    public ProjectMatchingAffiliationResponse getMyAffiliation(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam(required = false) Long gisuId
    ) {
        return ProjectMatchingAffiliationResponse.from(
            getMyMatchingAffiliationUseCase.get(memberPrincipal.getMemberId(), gisuId)
        );
    }
}
