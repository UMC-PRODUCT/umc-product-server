package com.umc.product.test.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.SeedChallengersRequest;
import com.umc.product.test.adapter.in.web.dto.SeedChallengersResponse;
import com.umc.product.test.adapter.in.web.dto.SeedCurriculumRequest;
import com.umc.product.test.adapter.in.web.dto.SeedCurriculumResponse;
import com.umc.product.test.adapter.in.web.dto.SeedMembersRequest;
import com.umc.product.test.adapter.in.web.dto.SeedMembersResponse;
import com.umc.product.test.adapter.in.web.dto.SeedNoticeRequest;
import com.umc.product.test.adapter.in.web.dto.SeedNoticeResponse;
import com.umc.product.test.adapter.in.web.dto.SeedProjectsRequest;
import com.umc.product.test.adapter.in.web.dto.SeedProjectsResponse;
import com.umc.product.test.application.port.in.command.SeedChallengersUseCase;
import com.umc.product.test.application.port.in.command.SeedCurriculumUseCase;
import com.umc.product.test.application.port.in.command.SeedMembersUseCase;
import com.umc.product.test.application.port.in.command.SeedNoticeUseCase;
import com.umc.product.test.application.port.in.command.SeedProjectsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * test 도메인 시딩 API. ADR-017 참조.
 * <p>
 * @Profile("!prod") + app.seed.enabled=true 두 조건을 모두 만족할 때만 빈으로 등록된다.
 * prod 환경에서는 빈 등록 자체가 차단되어 외부 노출 가능성이 없다.
 */
@RestController
@RequestMapping("/test/seed")
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
@Public
@Tag(name = "Test | 시딩", description = "운영 외 환경 전용 시딩 API 입니다. prod 환경에서는 활성화되지 않습니다.")
public class SeedController {

    private final SeedMembersUseCase seedMembersUseCase;
    private final SeedChallengersUseCase seedChallengersUseCase;
    private final SeedProjectsUseCase seedProjectsUseCase;
    private final SeedCurriculumUseCase seedCurriculumUseCase;
    private final SeedNoticeUseCase seedNoticeUseCase;

    @Operation(
        summary = "[SEED-001] 더미 멤버 시딩",
        description = """
            ID/PW 더미 멤버를 N 명 즉시 생성합니다. 모든 더미 회원은 동일한 비밀번호
            (app.seed.default-password)를 사용합니다.
            force=false (기본) 이면 현재 회원 수가 임계값을 초과한 경우 시딩을 스킵합니다.
            force=true 이면 임계값 체크를 무시하고 무조건 시딩합니다.
            챌린저/프로젝트 시딩 전 호출이 필요합니다.
            """
    )
    @PostMapping("/members")
    public SeedMembersResponse seedMembers(@RequestBody @Valid SeedMembersRequest request) {
        return SeedMembersResponse.from(seedMembersUseCase.seed(request.toCommand()));
    }

    @Operation(
        summary = "[SEED-002] 챌린저 분포 시딩",
        description = """
            특정 기수에 대해 (Chapter, School, Part) 셀마다 countPerPartPerSchool 명의 더미 회원 +
            챌린저를 함께 생성합니다.
            gisuId 가 null 이면 활성 기수, parts 가 null/empty 면 ADMIN 제외 전 파트,
            chapterIds 가 null/empty 면 해당 기수의 모든 Chapter 가 대상입니다.
            """
    )
    @PostMapping("/challengers")
    public SeedChallengersResponse seedChallengers(@RequestBody @Valid SeedChallengersRequest request) {
        return SeedChallengersResponse.from(seedChallengersUseCase.seed(request.toCommand()));
    }

    @Operation(
        summary = "[SEED-003] 프로젝트 시딩",
        description = """
            활성 기수(또는 지정 기수)의 같은 school 멤버 풀에서 PLAN 1 + 프론트엔드 5~6 +
            백엔드 5~6 의 멤버 슬롯을 추출해 프로젝트를 N 개 생성합니다.
            School 단위 round-robin 으로 채우며, 풀이 부족한 셀은 skip 합니다.
            PO 후보가 PLAN 챌린저로 등록되지 않은 경우 시딩 측에서 PLAN 챌린저로 등록한 뒤
            createDraft 를 호출합니다.
            """
    )
    @PostMapping("/projects")
    public SeedProjectsResponse seedProjects(@RequestBody @Valid SeedProjectsRequest request) {
        return SeedProjectsResponse.from(seedProjectsUseCase.seed(request.toCommand()));
    }

    @Operation(
        summary = "[SEED-004] Curriculum 시딩 (Curriculum · WeeklyCurriculum · OriginalWorkbook · Mission)",
        description = """
            활성 기수(또는 지정 기수)에 대해 ADMIN 제외 파트별로 다음 골격을 시딩합니다.
            Curriculum (1/파트) → WeeklyCurriculum (1~N 주차) → OriginalWorkbook (MAIN, READY) → Mission (M개).
            releaseRequesterMemberId 가 지정되면 모든 워크북을 READY → RELEASED 로 전환합니다.
            각 단계별로 실패 격리되며, 단계별 실패 카운트가 응답에 포함됩니다.
            """
    )
    @PostMapping("/curriculum")
    public SeedCurriculumResponse seedCurriculum(@RequestBody @Valid SeedCurriculumRequest request) {
        return SeedCurriculumResponse.from(seedCurriculumUseCase.seed(request.toCommand()));
    }

    @Operation(
        summary = "[SEED-005] Notice 시딩 (지부 · 학교 · 파트 분포)",
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
}
