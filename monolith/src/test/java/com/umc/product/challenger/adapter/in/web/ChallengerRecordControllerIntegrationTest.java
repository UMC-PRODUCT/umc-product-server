package com.umc.product.challenger.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import com.umc.product.authentication.application.port.out.SaveMemberOAuthPort;
import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.authorization.adapter.out.persistence.ChallengerRoleJpaRepository;
import com.umc.product.challenger.adapter.in.web.assembler.ChallengerRecordResponseAssembler;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerRecordResponse;
import com.umc.product.challenger.adapter.out.persistence.ChallengerJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerRecordJpaRepository;
import com.umc.product.challenger.application.port.in.command.ManageChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ConsumeChallengerRecordCommand;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerRecordPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.command.SaveGisuPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace.TransactionCall;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;

@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChallengerRecordController 통합 테스트")
class ChallengerRecordControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private ChapterFixture chapterFixture;

    @Autowired
    private ChallengerFixture challengerFixture;

    @Autowired
    private SchoolFixture schoolFixture;

    @Autowired
    private SaveMemberPort saveMemberPort;

    @Autowired
    private SaveMemberOAuthPort saveMemberOAuthPort;

    @Autowired
    private SaveChallengerPort saveChallengerPort;

    @Autowired
    private SaveChallengerRecordPort saveChallengerRecordPort;

    @Autowired
    private ChallengerJpaRepository challengerJpaRepository;

    @Autowired
    private ChallengerRecordJpaRepository challengerRecordJpaRepository;

    @Autowired
    private ChallengerRoleJpaRepository challengerRoleJpaRepository;

    @Autowired
    private ManageChallengerRecordUseCase manageChallengerRecordUseCase;

    @Autowired
    private ChallengerRecordResponseAssembler recordResponseAssembler;

    @Autowired
    private SaveGisuPort saveGisuPort;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("일반 챌린저 기록 코드를 소비하면 챌린저가 생성되고 코드가 사용 처리된다")
    void 일반_챌린저_기록_코드를_소비하면_챌린저가_생성되고_코드가_사용_처리된다() throws Exception {
        // given
        RecordContext context = recordContext(9201L, "일반코드");
        Member member = member("홍길동", "길동", "regular-code@test.com", context.school().getId());
        Member creator = member("관리자", "관리", "regular-code-admin@test.com", context.school().getId());
        ChallengerRecord record = saveChallengerRecordPort.save(ChallengerRecord.create(
            creator.getId(),
            context.gisu().getId(),
            context.chapter().getId(),
            context.school().getId(),
            ChallengerPart.SPRINGBOOT,
            member.getName()
        ));
        authenticate(member.getId());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        Challenger savedChallenger = challengerJpaRepository
            .findByMemberIdAndGisuId(member.getId(), context.gisu().getId())
            .orElseThrow();
        assertThat(savedChallenger.getPart()).isEqualTo(ChallengerPart.SPRINGBOOT);

        ChallengerRecord usedRecord = challengerRecordJpaRepository.findByCode(record.getCode()).orElseThrow();
        assertThat(usedRecord.isUsed()).isTrue();
        assertThat(usedRecord.getUsedMemberId()).isEqualTo(member.getId());
        assertThat(usedRecord.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("운영진 기록 코드를 소비하면 기존 챌린저에 역할이 부여되고 새 챌린저는 만들지 않는다")
    void 운영진_기록_코드를_소비하면_기존_챌린저에_역할이_부여되고_새_챌린저는_만들지_않는다() throws Exception {
        // given
        RecordContext context = recordContext(9202L, "운영진코드");
        Member member = member("김운영", "운영", "admin-code@test.com", context.school().getId());
        Member creator = member("관리자", "관리", "admin-code-admin@test.com", context.school().getId());
        Challenger challenger = saveChallengerPort.save(Challenger.builder()
            .memberId(member.getId())
            .part(ChallengerPart.WEB)
            .gisuId(context.gisu().getId())
            .build());
        ChallengerRecord record = saveChallengerRecordPort.save(ChallengerRecord.createAdmin(
            creator.getId(),
            context.gisu().getId(),
            context.chapter().getId(),
            context.school().getId(),
            ChallengerPart.WEB,
            member.getName(),
            ChallengerRoleType.SCHOOL_PRESIDENT,
            context.school().getId()
        ));
        authenticate(member.getId());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        assertThat(challengerJpaRepository.findByMemberId(member.getId()))
            .extracting(Challenger::getId)
            .containsExactly(challenger.getId());

        assertThat(challengerRoleJpaRepository.findByChallengerId(challenger.getId()))
            .hasSize(1)
            .first()
            .satisfies(role -> {
                assertThat(role.getChallengerRoleType()).isEqualTo(ChallengerRoleType.SCHOOL_PRESIDENT);
                assertThat(role.getOrganizationId()).isEqualTo(context.school().getId());
                assertThat(role.getGisuId()).isEqualTo(context.gisu().getId());
            });

        ChallengerRecord usedRecord = challengerRecordJpaRepository.findByCode(record.getCode()).orElseThrow();
        assertThat(usedRecord.isUsed()).isTrue();
        assertThat(usedRecord.getUsedMemberId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("이미 사용된 챌린저 기록 코드는 재사용할 수 없다")
    void 이미_사용된_챌린저_기록_코드는_재사용할_수_없다() throws Exception {
        // given
        RecordContext context = recordContext(9203L, "재사용실패");
        Member member = member("박실패", "실패", "reused-code@test.com", context.school().getId());
        Member creator = member("관리자", "관리", "reused-code-admin@test.com", context.school().getId());
        ChallengerRecord record = saveChallengerRecordPort.save(ChallengerRecord.create(
            creator.getId(),
            context.gisu().getId(),
            context.chapter().getId(),
            context.school().getId(),
            ChallengerPart.ANDROID,
            member.getName()
        ));
        authenticate(member.getId());

        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isOk());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("CHALLENGER-0012"));

        assertThat(challengerJpaRepository.findByMemberId(member.getId()))
            .hasSize(1)
            .first()
            .extracting(Challenger::getPart)
            .isEqualTo(ChallengerPart.ANDROID);

        ChallengerRecord usedRecord = challengerRecordJpaRepository.findByCode(record.getCode()).orElseThrow();
        assertThat(usedRecord.isUsed()).isTrue();
        assertThat(usedRecord.getUsedMemberId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("비활성 기수의 코드를 발급하고 등록하면 기본 파트와 인프라 수강 여부가 저장된다")
    void 비활성_기수의_코드를_발급하고_등록하면_기본_파트와_인프라_수강_여부가_저장된다() throws Exception {
        // given
        Gisu gisu = saveGisuPort.save(Gisu.create(
            9301L, Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2027-02-01T00:00:00Z"),
            false));
        Chapter chapter = chapterFixture.지부(gisu, "트랙코드지부");
        School school = schoolFixture.지부에_소속된_학교("트랙코드학교", chapter);
        Member member = member("트랙회원", "트랙", "track-code@test.com", school.getId());
        Long recordId = manageChallengerRecordUseCase.create(CreateChallengerRecordCommand.builder()
            .creatorMemberId(member.getId()).gisuId(gisu.getId()).chapterId(chapter.getId())
            .schoolId(school.getId()).part(ChallengerPart.WEB_PRODUCT_ENGINEER).infra(true)
            .memberName(member.getName()).build());
        ChallengerRecord record = challengerRecordJpaRepository.findById(recordId).orElseThrow();
        ChallengerRecordResponse response = recordResponseAssembler.from(recordId);
        assertThat(response.chapterId()).isEqualTo(chapter.getId());
        assertThat(response.chapterName()).isEqualTo(chapter.getName());
        assertThat(response.part()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(response.infra()).isTrue();
        authenticate(member.getId());

        // when
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON).content(codeRequest(record.getCode())))
            .andExpect(status().isOk());

        // then
        Challenger challenger = challengerJpaRepository.findByMemberIdAndGisuId(member.getId(), gisu.getId())
            .orElseThrow();
        assertThat(challenger.getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(challenger.isInfra()).isTrue();
        assertThat(challengerRecordJpaRepository.findById(recordId).orElseThrow().isUsed()).isTrue();
    }

    @Test
    @DisplayName("지부 없는 비수강 중앙 운영진 코드를 조회하면 학교와 역할을 보존하고 지부는 비운다")
    void 지부_없는_비수강_중앙_운영진_코드를_조회한다() {
        // Given
        Gisu gisu = saveGisuPort.save(Gisu.create(
            9303L, Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2027-02-01T00:00:00Z"),
            false));
        School school = schoolFixture.학교("지부미배정중앙학교");
        Member member = member("중앙회원", "중앙", "central-code@test.com", school.getId());
        Long recordId = manageChallengerRecordUseCase.create(CreateChallengerRecordCommand.builder()
            .creatorMemberId(member.getId()).gisuId(gisu.getId()).schoolId(school.getId())
            .memberName(member.getName())
            .challengerRoleType(ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER).build());

        // When
        ChallengerRecordResponse response = recordResponseAssembler.from(recordId);

        // Then
        assertThat(response.chapterId()).isNull();
        assertThat(response.chapterName()).isNull();
        assertThat(response.schoolId()).isEqualTo(school.getId());
        assertThat(response.schoolName()).isEqualTo(school.getName());
        assertThat(response.part()).isNull();
        assertThat(response.infra()).isFalse();
        assertThat(response.challengerRoleType()).isEqualTo(ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER);
    }

    @Test
    @DisplayName("같은 코드의 동시 등록은 첫 트랜잭션을 기다린 뒤 이미 사용된 코드로 거절한다")
    void 같은_코드의_동시_등록은_첫_트랜잭션을_기다린_뒤_이미_사용된_코드로_거절한다() throws Exception {
        // given
        RecordContext context = recordContext(9302L, "코드동시성");
        Member firstMember = member("동명이인", "첫회원", "code-race-first@test.com", context.school().getId());
        Member secondMember = member("동명이인", "둘회원", "code-race-second@test.com", context.school().getId());
        ChallengerRecord record = saveChallengerRecordPort.save(ChallengerRecord.create(
            firstMember.getId(), context.gisu().getId(), context.chapter().getId(), context.school().getId(),
            ChallengerPart.WEB, firstMember.getName()));
        CountDownLatch firstConsumed = new CountDownLatch(1);
        CountDownLatch allowFirstCommit = new CountDownLatch(1);

        try (PostgreSqlTransactionRace race = new PostgreSqlTransactionRace(transactionManager, jdbcTemplate)) {
            TransactionCall<Void> first = race.submit(() -> {
                manageChallengerRecordUseCase.consumeCode(ConsumeChallengerRecordCommand.builder()
                    .targetMemberId(firstMember.getId()).code(record.getCode()).build());
                firstConsumed.countDown();
                race.await(allowFirstCommit, "첫 코드 소비 커밋 대기");
                return null;
            });
            race.await(firstConsumed, "첫 코드 소비 완료");

            // when
            TransactionCall<Void> second = race.submit(() -> {
                manageChallengerRecordUseCase.consumeCode(ConsumeChallengerRecordCommand.builder()
                    .targetMemberId(secondMember.getId()).code(record.getCode()).build());
                return null;
            });
            try {
                race.awaitPostgreSqlLockWait(second);
                assertThat(second.future().isDone()).isFalse();
            } finally {
                allowFirstCommit.countDown();
            }
            first.future().get(10, TimeUnit.SECONDS);

            // then
            ExecutionException failure = assertThrows(ExecutionException.class,
                () -> second.future().get(10, TimeUnit.SECONDS));
            assertThat(failure.getCause()).isInstanceOf(ChallengerDomainException.class)
                .extracting(error -> ((ChallengerDomainException) error).getBaseCode())
                .isEqualTo(ChallengerErrorCode.USED_CHALLENGER_RECORD_CODE);
        }
        assertThat(challengerJpaRepository.findByMemberIdAndGisuId(firstMember.getId(), context.gisu().getId()))
            .isPresent();
        assertThat(challengerJpaRepository.findByMemberIdAndGisuId(secondMember.getId(), context.gisu().getId()))
            .isEmpty();
        assertThat(challengerRecordJpaRepository.findById(record.getId()).orElseThrow().getUsedMemberId())
            .isEqualTo(firstMember.getId());
    }

    @Test
    @DisplayName("기록 없는 새 계정은 같은 학교와 이름의 다른 계정에 챌린저 기록이 있으면 코드를 쓸 수 없다")
    void 기록_없는_새_계정은_같은_학교와_이름의_다른_계정에_챌린저_기록이_있으면_코드를_쓸_수_없다() throws Exception {
        // given
        RecordContext context = recordContext(9211L, "중복차단");
        Member oldMember = member("중복회원", "기존", "dup-old@test.com", context.school().getId());
        challengerFixture.웹(oldMember.getId(), context.gisu().getId());
        Member newMember = member("중복회원", "새계정", "dup-new@test.com", context.school().getId());
        ChallengerRecord record = normalRecord(context, newMember);
        authenticate(newMember.getId());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("CHALLENGER-0023"));

        assertThat(challengerJpaRepository.findByMemberId(newMember.getId())).isEmpty();
        assertThat(challengerRecordJpaRepository.findByCode(record.getCode()).orElseThrow().isUsed()).isFalse();
    }

    @Test
    @DisplayName("코드 사용이 막히면 기록 있는 계정마다 가입 수단과 가린 이메일을 message로 안내한다")
    void 코드_사용이_막히면_기록_있는_계정마다_가입_수단과_가린_이메일을_message로_안내한다() throws Exception {
        // given
        RecordContext context = recordContext(9212L, "중복안내");
        Member socialMember = member("안내회원", "소셜", "kakao-old@test.com", context.school().getId());
        oauth(socialMember, OAuthProvider.KAKAO);
        oauth(socialMember, OAuthProvider.GOOGLE);
        challengerFixture.웹(socialMember.getId(), context.gisu().getId());
        Member emailMember = member("안내회원", "메일", "mail-old@test.com", context.school().getId());
        emailMember.registerCredential("{noop}password");
        saveMemberPort.save(emailMember);
        challengerFixture.웹(emailMember.getId(), gisuFixture.비활성_기수(9213L).getId());
        Member unknownMethodMember = member("안내회원", "수단없음", "bare-old@test.com", context.school().getId());
        challengerFixture.웹(unknownMethodMember.getId(), gisuFixture.비활성_기수(9219L).getId());
        Member newMember = member("안내회원", "새계정", "guide-new@test.com", context.school().getId());
        ChallengerRecord record = normalRecord(context, newMember);
        authenticate(newMember.getId());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(
                "구글·카카오로 가입한 계정(kak******@test.com), 이메일로 가입한 계정(mai*****@test.com), "
                    + "가입한 계정(bar*****@test.com)에 활동 기록이 있어요. "
                    + "그 계정으로 로그인해서 코드를 입력해주세요. 본인 계정이 아니면 운영진에게 문의해주세요."));
    }

    @Test
    @DisplayName("같은 학교와 이름의 다른 계정에 챌린저 기록이 없으면 새 계정도 코드를 쓸 수 있다")
    void 같은_학교와_이름의_다른_계정에_챌린저_기록이_없으면_새_계정도_코드를_쓸_수_있다() throws Exception {
        // given
        RecordContext context = recordContext(9214L, "기록없는중복");
        member("무기록회원", "기존", "no-record-old@test.com", context.school().getId());
        Member newMember = member("무기록회원", "새계정", "no-record-new@test.com", context.school().getId());
        ChallengerRecord record = normalRecord(context, newMember);
        authenticate(newMember.getId());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isOk());

        assertThat(challengerJpaRepository.findByMemberIdAndGisuId(newMember.getId(), context.gisu().getId()))
            .isPresent();
    }

    @Test
    @DisplayName("코드를 쓰는 계정에 이미 챌린저 기록이 있으면 다른 계정에 기록이 있어도 역할을 추가한다")
    void 코드를_쓰는_계정에_이미_챌린저_기록이_있으면_다른_계정에_기록이_있어도_역할을_추가한다() throws Exception {
        // given
        RecordContext context = recordContext(9215L, "기존계정");
        Member member = member("갈라진회원", "본계정", "split-main@test.com", context.school().getId());
        Challenger challenger = challengerFixture.웹(member.getId(), context.gisu().getId());
        Member otherMember = member("갈라진회원", "다른계정", "split-other@test.com", context.school().getId());
        challengerFixture.웹(otherMember.getId(), gisuFixture.비활성_기수(9216L).getId());
        ChallengerRecord record = saveChallengerRecordPort.save(ChallengerRecord.createAdmin(
            member.getId(), context.gisu().getId(), context.chapter().getId(), context.school().getId(),
            ChallengerPart.WEB, member.getName(), ChallengerRoleType.SCHOOL_PRESIDENT, context.school().getId()));
        authenticate(member.getId());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isOk());

        assertThat(challengerRoleJpaRepository.findByChallengerId(challenger.getId()))
            .extracting(role -> role.getChallengerRoleType())
            .containsExactly(ChallengerRoleType.SCHOOL_PRESIDENT);
    }

    @Test
    @DisplayName("기록 있는 같은 학교와 이름의 계정이 휴면이어도 새 계정은 코드를 쓸 수 없다")
    void 기록_있는_같은_학교와_이름의_계정이_휴면이어도_새_계정은_코드를_쓸_수_없다() throws Exception {
        // given
        RecordContext context = recordContext(9220L, "휴면계정");
        Member inactiveMember = member("휴면회원", "기존", "inactive-old@test.com", context.school().getId());
        challengerFixture.웹(inactiveMember.getId(), gisuFixture.비활성_기수(9221L).getId());
        ReflectionTestUtils.setField(inactiveMember, "status", MemberStatus.INACTIVE);
        saveMemberPort.save(inactiveMember);
        Member newMember = member("휴면회원", "새계정", "inactive-new@test.com", context.school().getId());
        ChallengerRecord record = normalRecord(context, newMember);
        authenticate(newMember.getId());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("CHALLENGER-0023"));

        assertThat(challengerJpaRepository.findByMemberId(newMember.getId())).isEmpty();
    }

    @Test
    @DisplayName("기록 있는 같은 학교와 이름의 계정이 탈퇴했으면 새 계정도 코드를 쓸 수 있다")
    void 기록_있는_같은_학교와_이름의_계정이_탈퇴했으면_새_계정도_코드를_쓸_수_있다() throws Exception {
        // given
        RecordContext context = recordContext(9217L, "탈퇴계정");
        Member withdrawnMember = member("탈퇴회원", "기존", "withdrawn-old@test.com", context.school().getId());
        challengerFixture.웹(withdrawnMember.getId(), gisuFixture.비활성_기수(9218L).getId());
        ReflectionTestUtils.setField(withdrawnMember, "status", MemberStatus.WITHDRAWN);
        saveMemberPort.save(withdrawnMember);
        Member newMember = member("탈퇴회원", "새계정", "withdrawn-new@test.com", context.school().getId());
        ChallengerRecord record = normalRecord(context, newMember);
        authenticate(newMember.getId());

        // when & then
        mockMvc.perform(post("/api/v1/challenger-record/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(codeRequest(record.getCode())))
            .andExpect(status().isOk());

        assertThat(challengerJpaRepository.findByMemberIdAndGisuId(newMember.getId(), context.gisu().getId()))
            .isPresent();
    }

    private ChallengerRecord normalRecord(RecordContext context, Member target) {
        return saveChallengerRecordPort.save(ChallengerRecord.create(
            target.getId(), context.gisu().getId(), context.chapter().getId(), context.school().getId(),
            ChallengerPart.WEB, target.getName()));
    }

    private void oauth(Member member, OAuthProvider provider) {
        saveMemberOAuthPort.save(MemberOAuth.builder()
            .memberId(member.getId())
            .provider(provider)
            .providerId(UUID.randomUUID().toString())
            .build());
    }

    private RecordContext recordContext(Long generation, String prefix) {
        Gisu gisu = gisuFixture.비활성_기수(generation);
        Chapter chapter = chapterFixture.지부(gisu, prefix + "지부");
        School school = schoolFixture.지부에_소속된_학교(prefix + "학교", chapter);
        return new RecordContext(gisu, chapter, school);
    }

    private Member member(String name, String nickname, String email, Long schoolId) {
        return saveMemberPort.save(Member.create(name, nickname, email, schoolId, null));
    }

    private void authenticate(Long memberId) {
        MemberPrincipal principal = MemberPrincipal.builder()
            .memberId(memberId)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private String codeRequest(String code) {
        return """
            {"code":"%s"}
            """.formatted(code);
    }

    private record RecordContext(Gisu gisu, Chapter chapter, School school) {
    }
}
