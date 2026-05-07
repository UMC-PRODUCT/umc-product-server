package com.umc.product.project.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.adapter.in.web.assembler.ProjectApplicationResponseAssembler;
import com.umc.product.project.adapter.in.web.dto.response.MyProjectApplicationResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicantResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectApplicationDetailResponse;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 지원서 Query", description = "프로젝트 지원서 및 지원자, 본인 지원 내역 조회")
public class ProjectApplicationQueryController {

    private final ProjectApplicationResponseAssembler assembler;

    @GetMapping("/me/applications")
    @Operation(
        summary = "[APPLY-004] 본인 지원 내역 목록 조회",
        description = """
            요청자의 챌린저 파트 기준으로 매칭 종류를 자동 결정해 본인 지원 내역을 조회한다.

            정렬: 매칭 라운드 시작일 ASC -> 지원서 갱신일 DESC.
            <p>
            `status` 파라미터 :
            <ul>
              <li>미지정 -> PENDING(임시저장) 제외 전체 (SUBMITTED/APPROVED/REJECTED)</li>
              <li>명시 시 해당 상태만 조회</li>
            </ul>
            <p>
            요청자가 해당 기수 챌린저가 아니거나 PLAN 또는 ADMIN 이면 빈 리스트를 반환한다.
            """
    )
    public List<MyProjectApplicationResponse> getMyApplications(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestParam Long gisuId,
        @RequestParam(required = false) ProjectApplicationStatus status
    ) {
        GetMyProjectApplicationsQuery query = GetMyProjectApplicationsQuery.builder()
            .requesterMemberId(memberPrincipal.getMemberId())
            .gisuId(gisuId)
            .status(status)
            .build();

        return assembler.myApplicationsFor(query);
    }

    @GetMapping("/{projectId}/applications")
    @Operation(
        summary = "[APPLY-101] PM/운영진 단일 프로젝트 지원자 목록 조회",
        description = """
            단일 프로젝트의 지원자 목록을 조회한다. 임시저장(PENDING) 지원서는 제외되며, SUBMITTED/APPROVED/REJECTED 만 노출.

            정렬: matchingRound.phase ASC -> submittedAt ASC. 같은 파트끼리 묶기는 클라이언트가 처리한다.
            <p>
            필터:
            <ul>
              <li>matchingRoundId -- 매칭 차수 단일 필터</li>
              <li>part -- 지원자(챌린저) 의 파트 필터</li>
              <li>status -- 지원 상태 (SUBMITTED / APPROVED / REJECTED). 미지정 시 전체.</li>
            </ul>
            <p>
            TODO: 권한 검사 (@CheckAccess) 미적용. 운영 배포 전 PM/Sub-PO/지부장/학교장/Central Core 분기 추가 필요.
            """
    )
    public List<ProjectApplicantResponse> getProjectApplicants(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @RequestParam(required = false) Long matchingRoundId,
        @RequestParam(required = false) ChallengerPart part,
        @RequestParam(required = false) ProjectApplicationStatus status
    ) {
        SearchProjectApplicationsQuery query = SearchProjectApplicationsQuery.builder()
            .projectId(projectId)
            .matchingRoundId(matchingRoundId)
            .part(part)
            .status(status)
            .build();

        return assembler.applicantsFor(query);
    }

    @GetMapping("/{projectId}/applications/{applicationId}")
    @Operation(
        summary = "[APPLY-102] 지원서 단건 상세 조회",
        description = """
            지원서 단건의 메타(지원자/매칭 차수/상태/시각) + 폼 구조(지원자 파트 기준 마스킹) + 제출된 답변 본문 + 첨부 파일 메타까지 한 번에 반환한다.
            <p>
            노출 규칙:
            <ul>
              <li>SUBMITTED/APPROVED/REJECTED -- 4종 호출자(PO/Sub-PO/지부장/CC/지원자 본인) 모두 동일 응답</li>
              <li>PENDING(임시저장) -- 지원자 본인만 조회 가능. 그 외 호출자에게는 404(PROJECT-0021) 로 위장하여 임시저장본의 존재 자체를 은닉</li>
            </ul>
            <p>
            정합성: path 의 projectId 와 application 의 form.project.id 가 다르면 404(PROJECT-0021) 로 위장하여 다른 프로젝트의 지원서 존재 여부를 은닉한다.
            <p>
            TODO: 4종 호출자(@CheckAccess) 자격 검증은 추후에 추가된다 -- 현재 이 엔드포인트는 인증된 사용자라면 누구나 호출 가능.
            """
    )
    public ProjectApplicationDetailResponse getApplicationDetail(
        @CurrentMember MemberPrincipal memberPrincipal,
        @PathVariable Long projectId,
        @PathVariable Long applicationId
    ) {
        GetProjectApplicationDetailQuery query = GetProjectApplicationDetailQuery.builder()
            .projectId(projectId)
            .applicationId(applicationId)
            .requesterMemberId(memberPrincipal.getMemberId())
            .build();

        return assembler.detailFor(query);
    }
}
