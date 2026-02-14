package com.umc.product.test.controller;

import com.google.common.collect.Lists;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateChapterCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.terms.application.port.in.command.ManageTermsUseCase;
import com.umc.product.terms.application.port.in.command.dto.CreateTermCommand;
import com.umc.product.terms.application.port.in.query.GetTermsUseCase;
import com.umc.product.terms.domain.enums.TermsType;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Public
@RequiredArgsConstructor
@Profile("local | dev")
@Tag(name = "Test | 더미 데이터", description = "잘못 쓰면 DB 날라갑니다")
@Slf4j
@RestController
@RequestMapping("/test/mock-data")
public class MockDataController {

    private final ManageGisuUseCase manageGisuUseCase;
    private final ManageChapterUseCase manageChapterUseCase;
    private final ManageSchoolUseCase manageSchoolUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;
    private final ManageMemberUseCase manageMemberUseCase;
    private final ManageTermsUseCase manageTermsUseCase;

    private final GetTermsUseCase getTermsUseCase;

    private static final Faker faker = new Faker(Locale.KOREAN);

    List<Long> createdGisu = new ArrayList<>();
    List<Long> createdSchool = new ArrayList<>();
    List<Long> createdChallengers = new ArrayList<>();

    Map<Long, List<Long>> gisuToChaptersMap = new HashMap<>();
    Map<Long, List<Long>> schoolToMembersMap = new HashMap<>();

    @PostMapping("all")
    void createDummyData() {
        log.info("[MOCK DATA] 약관 Mock Data를 생성합니다.");
        createTerms();

        log.info("[MOCK DATA] 기수 Mock Data를 생성합니다.");
        createGisu();

        log.info("[MOCK DATA] 학교 Mock Data를 생성합니다.");
        createSchools();

        log.info("[MOCK DATA] 학교-지부 관계 Mock Data를 생성합니다.");
        for (Long gisuId : createdGisu) {
            createChaptersInGisu(gisuId, 3);
        }

        log.info("[MOCK DATA] 회원 Mock Data를 생성합니다.");
        createMembers(200);

        log.info("[MOCK DATA] 챌린저 기록 Mock Data를 생성합니다.");
        createChallengerRecordToMembers();

        log.info("[MOCK DATA] 챌린저 상벌점 기록 Mock Data를 생성합니다.");
        grantChallengerPointRandom();
    }

    // PRIVATE METHODS

    private void createTerms() {
        // 개인정보처리방침
        manageTermsUseCase.createTerms(
            CreateTermCommand.builder()
                .link("https://makeus-challenge.notion.site/300b57f4596b803f8c94dd4f4fb71960?source=copy_link")
                .type(TermsType.PRIVACY)
                .required(true)
                .build()
        );

        // 서비스 이용약관
        manageTermsUseCase.createTerms(
            CreateTermCommand.builder()
                .link("https://makeus-challenge.notion.site/300b57f4596b8018a2dfd38784478715?source=copy_link")
                .type(TermsType.SERVICE)
                .required(true)
                .build()
        );

        // 마케팅정보수신동의
        manageTermsUseCase.createTerms(
            CreateTermCommand.builder()
                .link("https://makeus-challenge.notion.site/300b57f4596b808193c0c4d2d8e2f785?source=copy_link")
                .type(TermsType.MARKETING)
                .required(false)
                .build()
        );
    }

    private void createGisu() {
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

        for (CreateGisuCommand gisuCommand : gisuCommands) {
            createdGisu.add(manageGisuUseCase.register(gisuCommand));
        }
    }

    /**
     * 학교를 먼저 생성하고 진행해야 합니다.
     */
    private void createChaptersInGisu(Long gisuId, int numberOfChapters) {
        // 각 챕터당 할당할 학교 수 계산
        int chunkSize = (int) Math.ceil((double) createdSchool.size() / numberOfChapters);

        // Guava의 partition으로 나누기
        List<List<Long>> partitionedSchools = Lists.partition(createdSchool, chunkSize);

        for (int i = 0; i < numberOfChapters; i++) {
            List<Long> schoolIds = i < partitionedSchools.size()
                ? partitionedSchools.get(i)
                : List.of();  // 학교가 부족하면 빈 리스트

            Long createdChapter = manageChapterUseCase.create(
                CreateChapterCommand.builder()
                    .gisuId(gisuId)
                    .name(faker.word().noun())
                    .schoolIds(schoolIds)
                    .build()
            );

            gisuToChaptersMap.computeIfAbsent(gisuId, k -> new ArrayList<>()).add(createdChapter);
        }
    }

    private void createSchools() {
        List<String> schoolNames = List.of(
            // 샤라웃 투 1st 프로덕트팀 ...
            "중앙대학교", // 하늘, 스읍, 라엘, 벨라, 제옹
            "동국대학교", // 박박지현, 갈래
            "홍익대학교 서울캠퍼스", // 쳇쳇
            "한양대학교 ERICA", // 와나
            "숭실대학교", // 어헛차
            "명지대학교", // 조나단
            "상명대학교", // 코튼
            "가천대학교", // 소피
            "한성대학교", // 리버
            "서울여자대학교", // 나루, 도리
            "성신여자대학교", // 삼이
            "덕성여자대학교", // 마티
            "동덕여자대학교" // 세니
        );

        for (String schoolName : schoolNames) {
            Long schoolId = manageSchoolUseCase.register(
                CreateSchoolCommand.builder()
                    .schoolName(schoolName)
                    .remark("테스트용 더미 학교입니다.")
                    .build()
            );

            createdSchool.add(schoolId);
        }
    }

    private void createMembers(int memberPerSchool) {
        List<TermConsents> termConsentsList = getTermsUseCase.getRequiredTermIds().stream()
            .map(
                termId -> TermConsents.builder()
                    .termId(termId)
                    .isAgreed(true)
                    .build()
            ).toList();

        for (Long schoolId : createdSchool) {
            List<RegisterMemberCommand> memberCommands = new ArrayList<>();
            for (int i = 0; i < memberPerSchool; i++) {
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
    }

    /**
     * 각 기수에, 학교에 속한 회원 각각에 챌린저 기록을 생성한다.
     */
    private void createChallengerRecordToMembers() {
        for (Long gisuId : createdGisu) {
            createdChallengers.addAll(
                manageChallengerUseCase.createChallengerBulk(
                    // 각 학교에 속한 회원들을 쭉 나열
                    schoolToMembersMap.values().stream()
                        .flatMap(List::stream)
                        // 나열해서 command를 생성함
                        .map(memberId -> CreateChallengerCommand.builder()
                            .memberId(memberId)
                            .part(ChallengerPart.random())
                            .gisuId(gisuId)
                            .build())
                        .toList()
                )
            );
        }
    }

    /**
     * 생성한 챌린저 중 일부에게 임의로 ChallengerRole을 부여합니다.
     */
    private void createChallengerRoleRandom() {
        // TODO: 챌린저 역할을 랜덤으로 부여하는 로직 추가
    }

    /**
     * 임의로 상벌점을 각 챌린저들에게 부여합니다.
     */
    private void grantChallengerPointRandom() {
        List<GrantChallengerPointCommand> commands =
            createdChallengers.stream()
                .flatMap(challengerId -> {
                    int count = ThreadLocalRandom.current().nextInt(0, 7); // 0~6 랜덤
                    return IntStream.range(0, count)
                        .mapToObj(i -> GrantChallengerPointCommand.builder()
                            .challengerId(challengerId)
                            .pointType(PointType.random())
                            .description("더미 데이터 생성용 상벌점")
                            .build());
                })
                .toList();

        manageChallengerUseCase.grantChallengerPointBulk(commands);
    }
}
