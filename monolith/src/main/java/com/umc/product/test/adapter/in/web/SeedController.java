package com.umc.product.test.adapter.in.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.CreateSeedChallengerRequest;
import com.umc.product.test.adapter.in.web.dto.CreateSeedChallengerResponse;
import com.umc.product.test.adapter.in.web.dto.CreateSeedChallengerRoleRequest;
import com.umc.product.test.adapter.in.web.dto.CreateSeedChallengerRoleResponse;
import com.umc.product.test.adapter.in.web.dto.CreateSeedMemberRequest;
import com.umc.product.test.adapter.in.web.dto.CreateSeedMemberResponse;
import com.umc.product.test.adapter.in.web.dto.CreateSeedMemberSystemRoleRequest;
import com.umc.product.test.adapter.in.web.dto.CreateSeedMemberSystemRoleResponse;
import com.umc.product.test.adapter.in.web.dto.DeleteSeedProjectDataRequest;
import com.umc.product.test.adapter.in.web.dto.DeleteSeedProjectDataResponse;
import com.umc.product.test.adapter.in.web.dto.SeedChallengerPointsRequest;
import com.umc.product.test.adapter.in.web.dto.SeedChallengerPointsResponse;
import com.umc.product.test.adapter.in.web.dto.SeedChallengersRequest;
import com.umc.product.test.adapter.in.web.dto.SeedChallengersResponse;
import com.umc.product.test.adapter.in.web.dto.SeedCurriculumRequest;
import com.umc.product.test.adapter.in.web.dto.SeedCurriculumResponse;
import com.umc.product.test.adapter.in.web.dto.SeedNoticeRequest;
import com.umc.product.test.adapter.in.web.dto.SeedNoticeResponse;
import com.umc.product.test.application.port.in.command.CreateSeedChallengerRoleUseCase;
import com.umc.product.test.application.port.in.command.CreateSeedChallengerUseCase;
import com.umc.product.test.application.port.in.command.CreateSeedMemberSystemRoleUseCase;
import com.umc.product.test.application.port.in.command.CreateSeedMemberUseCase;
import com.umc.product.test.application.port.in.command.DeleteSeedProjectDataUseCase;
import com.umc.product.test.application.port.in.command.SeedChallengerPointsUseCase;
import com.umc.product.test.application.port.in.command.SeedChallengersUseCase;
import com.umc.product.test.application.port.in.command.SeedCurriculumUseCase;
import com.umc.product.test.application.port.in.command.SeedNoticeUseCase;
import com.umc.product.test.application.port.in.command.dto.DeleteSeedProjectDataCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * test 도메인 시딩 API. ADR-017 참조.
 * <p>
 *
 * @Profile("!prod") + app.seed.enabled=true 두 조건을 모두 만족할 때만 빈으로 등록된다. prod 환경에서는 빈 등록 자체가 차단되어 외부 노출 가능성이 없다.
 */
@RestController
@RequestMapping("/test/seed")
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
@Public
@Tag(name = "Test | 시딩", description = "운영 외 환경에서 시딩 데이터를 만듭니다. prod 환경에서는 비활성화됩니다.")
public class SeedController {

    private final CreateSeedMemberUseCase createSeedMemberUseCase;
    private final CreateSeedMemberSystemRoleUseCase createSeedMemberSystemRoleUseCase;
    private final SeedChallengersUseCase seedChallengersUseCase;
    private final CreateSeedChallengerUseCase createSeedChallengerUseCase;
    private final CreateSeedChallengerRoleUseCase createSeedChallengerRoleUseCase;
    private final DeleteSeedProjectDataUseCase deleteSeedProjectDataUseCase;
    private final SeedCurriculumUseCase seedCurriculumUseCase;
    private final SeedNoticeUseCase seedNoticeUseCase;
    private final SeedChallengerPointsUseCase seedChallengerPointsUseCase;

    @Operation(
        operationId = "SEED-001-M",
        summary = "테스트 멤버 단건 생성",
        description = """
            이름, 닉네임, 학교 ID, 이메일을 받아 ID/PW 멤버를 1명 생성합니다.
            rawPassword 를 생략하거나 공백으로 보내면 app.seed.default-password 를 사용합니다.
            활성 필수 약관은 모두 동의한 것으로 자동 처리합니다.
            """
    )
    @PostMapping("/member")
    public CreateSeedMemberResponse createMember(@RequestBody @Valid CreateSeedMemberRequest request) {
        return CreateSeedMemberResponse.from(createSeedMemberUseCase.create(request.toCommand()));
    }

    @Operation(
        operationId = "SEED-001-R",
        summary = "테스트 회원 시스템 역할 부여",
        description = """
            회원 ID와 시스템 역할을 받아 비운영 환경의 테스트 회원에게 역할을 부여합니다.
            지원되는 역할은 SUPER_ADMIN, UMC_PRODUCT이며, 같은 역할이 이미 있으면 새 행을 만들지 않습니다.
            """
    )
    @PostMapping("/member-system-role")
    public CreateSeedMemberSystemRoleResponse createMemberSystemRole(
        @RequestBody @Valid CreateSeedMemberSystemRoleRequest request
    ) {
        return CreateSeedMemberSystemRoleResponse.from(
            createSeedMemberSystemRoleUseCase.create(request.toCommand()));
    }

    @Operation(
        operationId = "SEED-002",
        summary = "챌린저 분포 시딩",
        description = """
            특정 기수의 학교별로 더미 회원과 챌린저를 함께 생성합니다.
            parts와 countPerPartPerSchool을 사용하며, parts 생략 시 PLAN, DESIGN, WEB_PRODUCT_ENGINEER, MOBILE_PRODUCT_ENGINEER가 대상입니다.
            gisuId 생략 시 활성 기수, chapterIds 생략 시 해당 기수의 모든 지부가 대상입니다.
            인프라는 파트가 아니라 챌린저의 infra 선택 트랙이므로 parts로 입력하지 않습니다.
            """
    )
    @PostMapping("/challengers")
    public SeedChallengersResponse seedChallengers(@RequestBody @Valid SeedChallengersRequest request) {
        return SeedChallengersResponse.from(seedChallengersUseCase.seed(request.toCommand()));
    }

    @Operation(
        operationId = "SEED-002-C",
        summary = "테스트 챌린저 단건 생성",
        description = """
            memberId와 gisuId로 챌린저 1명을 만듭니다.
            단일 part를 입력하고, 웹/모바일 프로덕트 엔지니어가 인프라 선택 트랙을 수강하면 infra=true를 입력합니다.
            기수 소속 생성만으로 운영진 권한이 부여되지는 않습니다.
            """
    )
    @PostMapping("/challenger")
    public CreateSeedChallengerResponse createChallenger(
        @RequestBody @Valid CreateSeedChallengerRequest request
    ) {
        return CreateSeedChallengerResponse.from(createSeedChallengerUseCase.create(request.toCommand()));
    }

    @Operation(
        operationId = "SEED-002-R",
        summary = "테스트 챌린저 역할 단건 생성",
        description = """
            challengerId, roleType, gisuId 를 받아 운영진 역할을 1개 부여합니다.
            중앙 운영진 역할은 organizationId 없이 생성할 수 있고,
            CHAPTER_PRESIDENT 는 organizationId 에 chapterId, SCHOOL_PRESIDENT 등 학교 역할은
            organizationId 에 schoolId 를 전달합니다.
            """
    )
    @PostMapping("/challenger-role")
    public CreateSeedChallengerRoleResponse createChallengerRole(
        @RequestBody @Valid CreateSeedChallengerRoleRequest request
    ) {
        return CreateSeedChallengerRoleResponse.from(createSeedChallengerRoleUseCase.create(request.toCommand()));
    }

    @Operation(
        operationId = "SEED-003-D",
        summary = "프로젝트 시딩 데이터 삭제",
        description = """
            특정 기수의 프로젝트 관련 데이터를 물리 삭제합니다.
            삭제 범위는 Project, ProjectMember, ProjectPartQuota, ProjectApplication,
            ProjectApplicationForm/Policy, 해당 기수 Chapter 의 ProjectMatchingRound,
            그리고 프로젝트 지원 폼이 생성한 form Form/FormSection/Question/QuestionOption/
            FormResponse/Answer/AnswerChoice/legacy SingleAnswer 입니다.
            gisuId 가 null 이면 활성 기수를 대상으로 합니다. prod 환경에서는 노출되지 않습니다.
            """
    )
    @DeleteMapping("/projects")
    public DeleteSeedProjectDataResponse deleteProjectData(
        @RequestBody(required = false) @Valid DeleteSeedProjectDataRequest request
    ) {
        DeleteSeedProjectDataCommand command = request == null
            ? DeleteSeedProjectDataCommand.of(null)
            : request.toCommand();
        return DeleteSeedProjectDataResponse.from(deleteSeedProjectDataUseCase.delete(command));
    }

    @Operation(
        operationId = "SEED-004",
        summary = "Curriculum 시딩 (Curriculum · WeeklyCurriculum · OriginalWorkbook · Mission)",
        description = """
            활성 기수(또는 지정 기수)의 파트별로 다음 골격을 시딩합니다.
            parts를 생략하면 PLAN, DESIGN, WEB_PRODUCT_ENGINEER, MOBILE_PRODUCT_ENGINEER를 사용합니다.
            Curriculum (1/파트) → WeeklyCurriculum (1~N 주차) → OriginalWorkbook (MAIN, READY) → Mission (M개).
            releaseRequesterMemberId 가 지정되면 모든 워크북을 READY → RELEASED 로 전환합니다.
            스터디 그룹 배정이나 개인 워크북 수령은 포함하지 않습니다.
            각 단계별로 실패 격리되며, 단계별 실패 카운트가 응답에 포함됩니다.
            """
    )
    @PostMapping("/curriculum")
    public SeedCurriculumResponse seedCurriculum(@RequestBody @Valid SeedCurriculumRequest request) {
        return SeedCurriculumResponse.from(seedCurriculumUseCase.seed(request.toCommand()));
    }

    @Operation(
        operationId = "SEED-005",
        summary = "Notice 시딩 (지부 · 학교 · 파트 분포)",
        description = """
            활성 기수(또는 지정 기수)에 대해 다음 4 가지 scope 로 공지를 분포 시딩합니다.
              GLOBAL  : 기수 전체 대상 (작성자에게 중앙 총괄단 권한 필요)
              CHAPTER : 각 지부별        (작성자에게 해당 지부 회장 권한 필요)
              SCHOOL  : 각 학교별        (작성자에게 해당 학교 회장단 권한 필요)
              PART    : 각 파트별        (작성자에게 중앙 운영진 권한 필요)
            각 공지의 제목과 내용에는 대상 범위 정보 ([전체]/[지부]/[학교]/[파트] + 식별자) 가
            포함되어 운영 화면에서 시딩 데이터 식별이 쉬워집니다.
            authorMemberId 의 권한이 부족한 scope 는 scopeBreakdown 의 failed 카운트에 잡힙니다.
            """
    )
    @PostMapping("/notice")
    public SeedNoticeResponse seedNotice(@RequestBody @Valid SeedNoticeRequest request) {
        return SeedNoticeResponse.from(seedNoticeUseCase.seed(request.toCommand()));
    }

    @Operation(
        operationId = "SEED-007",
        summary = "챌린저 상벌점 벌크 시딩",
        description = """
            챌린저 ID 목록을 받아 각 챌린저에게 countPerChallenger 건씩 상벌점을 부여합니다.
            타입은 상점 3종 + 벌점 2종(BLOG_CHALLENGE, PEER_REVIEW_SUBMISSION, BEST_WORKBOOK_V2,
            STUDY_LATE, NO_WORKBOOK_MISSION)을 순환하며 섞어 부여해 홈 화면 challengerHistory 에
            상점/벌점이 함께 보이도록 만듭니다. 대상 챌린저는 활동 중이거나 수료 상태여야 합니다.
            부여 방식은 단일 트랜잭션 벌크(grantChallengerPointBulk)라 건당 API 호출보다 빠릅니다.
            """
    )
    @PostMapping("/challenger-points")
    public SeedChallengerPointsResponse seedChallengerPoints(
        @RequestBody @Valid SeedChallengerPointsRequest request
    ) {
        return SeedChallengerPointsResponse.from(seedChallengerPointsUseCase.seed(request.toCommand()));
    }
}
