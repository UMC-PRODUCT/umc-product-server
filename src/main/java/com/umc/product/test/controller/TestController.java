package com.umc.product.test.controller;

import com.umc.product.authentication.adapter.out.external.AppleTokenVerifier;
import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import com.umc.product.organization.application.port.in.command.dto.AssignSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateChapterCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.terms.application.port.in.query.GetTermsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Profile("local | dev")
@RestController
@RequestMapping("/test")
@Tag(name = Constants.TEST)
@Slf4j
@Public
public class TestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final GetChallengerUseCase getChallengerUseCase;
    private final AppleTokenVerifier appleTokenVerifier;

    private final ManageGisuUseCase manageGisuUseCase;
    private final ManageChapterUseCase manageChapterUseCase;
    private final ManageSchoolUseCase manageSchoolUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;
    private final ManageMemberUseCase manageMemberUseCase;
    private final GetTermsUseCase getTermsUseCase;

    @GetMapping("dummy/data")
    @Transactional
    void createDummyData() {
        Faker faker = new Faker(Locale.KOREAN);

        // 기수를 3개 생성한다 - 한 개는 Active, 나머지는 Inactive 한거로
        List<CreateGisuCommand> gisuCommands = List.of(
            CreateGisuCommand.builder()
                .number(7L)
                .startAt(LocalDateTime.now().minusMonths(18).toInstant(ZoneOffset.UTC))
                .endAt(LocalDateTime.now().minusMonths(12).toInstant(ZoneOffset.UTC))
                .build(),
            CreateGisuCommand.builder()
                .number(8L)
                .startAt(LocalDateTime.now().minusMonths(12).toInstant(ZoneOffset.UTC))
                .endAt(LocalDateTime.now().minusMonths(6).toInstant(ZoneOffset.UTC))
                .build(),
            CreateGisuCommand.builder()
                .number(9L)
                .startAt(LocalDateTime.now().minusMonths(6).toInstant(ZoneOffset.UTC))
                .endAt(LocalDateTime.now().plusMonths(1).toInstant(ZoneOffset.UTC))
                .build()
        );

        List<Long> createdGisu = new ArrayList<>();
        List<Long> createdSchool = new ArrayList<>();

        for (CreateGisuCommand gisuCommand : gisuCommands) {
            createdGisu.add(manageGisuUseCase.register(gisuCommand));
        }

        // 기수별 지부 map
        Map<Long, List<Long>> gisuToChaptersMap = new HashMap<>();

        // 각 기수에 지부를 5개씩 생성한다
        for (Long gisuId : createdGisu) {
            for (int i = 0; i < 5; i++) {
                Long createdChapter = manageChapterUseCase.create(
                    CreateChapterCommand.builder()
                        .gisuId(gisuId)
                        .name(faker.word().noun())
                        .schoolIds(List.of())
                        .build()
                );

                gisuToChaptersMap.computeIfAbsent(gisuId, k -> new ArrayList<>()).add(createdChapter);
            }
        }

        // 학교를 생성한다
        for (int i = 0; i < 20; i++) {
            createdSchool.add(
                manageSchoolUseCase.register(
                    CreateSchoolCommand.builder()
                        .schoolName(faker.educator().university())
                        .remark("학교 Mock Data 중 " + i + "번째 학교 입니다.")
                        .build()
                    // 로고랑 링크는 null 값으로 포함
                )
            );
        }

        // 지부에 학교를 할당한다 (같은 기수 내에서는 중복해서 할당하지 않도록 주의할 것)
        for (Long gisuId : gisuToChaptersMap.keySet()) {
            List<Long> chapters = gisuToChaptersMap.get(gisuId);
            int schoolIndex = 0;

            for (Long chapterId : chapters) {
                // 각 지부에 4개씩 학교 할당
                for (int j = 0; j < 4; j++) {
                    if (schoolIndex >= createdSchool.size()) {
                        break;
                    }

                    Long schoolId = createdSchool.get(schoolIndex++);
                    manageSchoolUseCase.assignToChapter(
                        new AssignSchoolCommand(
                            schoolId, chapterId
                        )
                    );
                }
            }
        }

        // 멤버를 생성하고 학교에 할당한다 (각 학교 당 500명씩)

        Set<Long> requiredTerms = getTermsUseCase.getRequiredTermIds();
        List<TermConsents> termConsentsList = requiredTerms.stream().map(
            termId -> TermConsents.builder()
                .termId(termId)
                .isAgreed(true)
                .build()
        ).toList();

        Map<Long, List<Long>> schoolToMembersMap = new HashMap<>();

        for (Long schoolId : createdSchool) {
            List<RegisterMemberCommand> memberCommands = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                memberCommands.add(
                    RegisterMemberCommand.builder()
                        .provider(OAuthProvider.GOOGLE)
                        .providerId(faker.idNumber().valid())
                        .name(faker.name().fullName().replace(" ", ""))
                        .nickname(schoolId + "_" + i)
                        .email(faker.internet().emailAddress())
                        .schoolId(schoolId)
                        .termConsents(termConsentsList)
                        .build()
                );
            }

            List<Long> memberIds = manageMemberUseCase.registerMembers(memberCommands);
            schoolToMembersMap.put(schoolId, memberIds);
        }

        // 멤버마다 챌린저를 생성한다 (각 기수별로, 파트는 랜덤 배정)
        for (Long gisuId : createdGisu) {
            schoolToMembersMap.values().stream()
                .flatMap(List::stream)
                .forEach(memberId -> {
                    manageChallengerUseCase.createChallenger(
                        CreateChallengerCommand.builder()
                            .memberId(memberId)
                            .part(ChallengerPart.random())
                            .gisuId(gisuId)
                            .build()
                    );
                });
        }

        // (선택) 챌린저 상벌점 내역을 설정한다

    }

    @GetMapping("apple-client-secret")
    String getAppleClientSecret() {
        return appleTokenVerifier.generateClientSecret();
    }

    @GetMapping("permission/notice-read")
    @CheckAccess(
        resourceType = ResourceType.NOTICE,
        resourceId = "#noticeId", // SpEL 표현식 - 공부하세요!!
        permission = PermissionType.READ,
        message = "하나야 스트레스 많이 받을거야~ 자기 전에도 생각 날꺼야~ 도움 많이 될꺼야~"
    )
    void permissionTest(Long noticeId) {
    }

    @GetMapping("permission/no-evaluator-test")
    @CheckAccess(
        resourceType = ResourceType.CURRICULUM,
        resourceId = "#noticeId",
        permission = PermissionType.DELETE,
        message = "하나야 스트레스 많이 받을거야~ 자기 전에도 생각 날꺼야~ 도움 많이 될꺼야~"
    )
    void noEvaluatorForPermission(Long something) {
    }

    @GetMapping("challenger")
    @Operation(summary = "memberId와 gisuId로 챌린저 정보 조회")
    public ChallengerInfo getChallengerByMemberAndGisuId(
        Long memberId, Long gisuId
    ) {
        return getChallengerUseCase.getByMemberIdAndGisuId(
            memberId, gisuId
        );
    }

    @Operation(summary = "AccessToken 발급")
    @Public
    @GetMapping("/token/access/{memberId}")
    public String getAccessToken(@PathVariable("memberId") Long memberId) {
        return jwtTokenProvider.createAccessToken(memberId, null);
    }

    @Operation(summary = "RefreshToken 발급")
    @Public
    @GetMapping("/token/refresh/{memberId}")
    public String getRefreshToken(@PathVariable("memberId") Long memberId) {
        return jwtTokenProvider.createRefreshToken(memberId);
    }

    @Operation(summary = "EmailVerificationToken 발급")
    @Public
    @GetMapping("/token/email/{email}")
    public String getEmailVerification(@PathVariable("email") String email) {
        return jwtTokenProvider.createEmailVerificationToken(email);
    }

    @Operation(summary = "oAuthVerificationToken 발급")
    @Public
    @GetMapping("/token/oauth")
    public String getOAuthVerificationToken(OAuthProvider provider, String providerId, String email) {
        return jwtTokenProvider.createOAuthVerificationToken(email, provider, providerId);
    }


    @Operation(summary = "헬스 체크 API")
    @Public
    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }

    @Operation(summary = "인증된 사용자인지 여부를 확인합니다.", description = "인증되지 않은 사용자인 경우 401을 반환합니다.")
    @GetMapping("/check-authenticated")
    public ApiResponse<String> checkAuthenticated(@CurrentMember MemberPrincipal currentUser) {
        return ApiResponse.onSuccess(currentUser.toString());
    }

    @GetMapping("log-test")
    public void logTest() {
        log.trace("TRACE");
        log.debug("DEBUG");
        log.info("INFO");
        log.warn("WARN");
        log.error("ERROR");
    }
}
