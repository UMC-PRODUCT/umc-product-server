package com.umc.product.test.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.SeedChallengersRequest;
import com.umc.product.test.adapter.in.web.dto.SeedChallengersResponse;
import com.umc.product.test.adapter.in.web.dto.SeedMembersRequest;
import com.umc.product.test.adapter.in.web.dto.SeedMembersResponse;
import com.umc.product.test.adapter.in.web.dto.SeedProjectsRequest;
import com.umc.product.test.adapter.in.web.dto.SeedProjectsResponse;
import com.umc.product.test.application.port.in.command.SeedChallengersUseCase;
import com.umc.product.test.application.port.in.command.SeedMembersUseCase;
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
}
