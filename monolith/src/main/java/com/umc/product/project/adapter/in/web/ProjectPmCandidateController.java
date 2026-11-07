package com.umc.product.project.adapter.in.web;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.project.application.port.in.query.SearchProjectPmCandidateUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectPmCandidateInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects/admin/pm-candidates")
@RequiredArgsConstructor
@Tag(name = "Project | 프로젝트 등록")
public class ProjectPmCandidateController {
    private final SearchProjectPmCandidateUseCase searchProjectPmCandidateUseCase;

    @GetMapping
    @Operation(operationId = "PROJECT-PM-CANDIDATES-001", summary = "프로젝트 대리 등록용 PM 후보 조회", description = """
        메인 PM과 보조 PM 선택에 사용하는 전용 조회입니다. gisuId는 필수이며 해당 기수의 활성 PLAN 챌린저만 반환합니다.
        지부장은 본인 지부 전체 학교, 총괄·부총괄은 소속 기수 전체 지부, 최고관리자는 선택한 기수 전체 지부의 PM을 조회합니다.
        일반 PM·학교 운영진·운영국원·교육국원 역할만으로는 접근할 수 없으며 403(PROJECT-0012)을 반환합니다.
        keyword로 이름·닉네임을 부분 검색하며 앞뒤 공백과 영문 대소문자를 구분하지 않습니다.
        page는 0부터 시작하고 size 기본값은 20입니다. 정렬은 이름·회원 ID 오름차순으로 고정합니다.
        각 후보는 memberId·challengerId·name·nickname·schoolId·schoolName을 제공합니다.
        등록 요청의 productOwnerMemberId에는 memberId를 사용합니다.
        기존 프로젝트 보유 여부로 후보를 제외하지 않으며 초안이나 팀원 정보를 변경하지 않습니다. PM 본인 등록은 기존 API를 사용합니다.
        """)
    public PageResponse<ProjectPmCandidateInfo> search(
        @CurrentMember MemberPrincipal principal,
        @RequestParam Long gisuId,
        @RequestParam(required = false) String keyword,
        @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return PageResponse.of(
            searchProjectPmCandidateUseCase.search(principal.getMemberId(), gisuId, keyword, pageable));
    }
}
