package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.adapter.out.persistence.MemberJpaRepository;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.RegisterOAuthMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.OAuthRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.in.query.CheckNicknameAvailabilityUseCase;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.SchoolFixture;
import com.umc.product.term.application.port.in.query.GetTermUseCase;

@DisplayName("확정 닉네임 정책의 가입·탈퇴·동시성")
class MemberNicknamePolicyIntegrationTest extends IntegrationTestSupport {

    @Autowired RegisterEmailMemberUseCase registerEmail;
    @Autowired RegisterOAuthMemberUseCase registerOAuth;
    @Autowired ManageMemberUseCase manageMember;
    @Autowired CheckNicknameAvailabilityUseCase availability;
    @Autowired SaveMemberPort saveMemberPort;
    @Autowired MemberJpaRepository memberRepository;
    @Autowired GetTermUseCase getTermUseCase;
    @Autowired SchoolFixture schools;
    @Autowired GisuFixture gisus;
    @Autowired ChallengerFixture challengers;
    @Autowired SaveChallengerPort saveChallengerPort;

    @Test
    @DisplayName("이메일 가입은 사전 확인을 생략해도 기존 중복과 가입 데이터 저장을 차단한다")
    void 이메일_중복_가입을_차단한다() {
        // given
        School school = schools.학교("가입대학교");
        saveMemberPort.save(Member.create("기존회원", "토리", "old@test.com", school.getId(), null));
        long before = memberRepository.count();
        var command = email(school.getId(), "토리", "new@test.com");

        // when & then
        assertNicknameConflict(() -> registerEmail.register(command));
        assertThat(memberRepository.count()).isEqualTo(before);
        assertThat(memberRepository.findByEmail("new@test.com")).isEmpty();
    }

    @Test
    @DisplayName("OAuth 가입에도 같은 학교 중복 제한을 적용한다")
    void 소셜_중복_가입을_차단한다() {
        // given
        School school = schools.학교("소셜대학교");
        saveMemberPort.save(Member.create("기존회원", "토리", "old@test.com", school.getId(), null));
        long before = memberRepository.count();
        var command = oauth(school.getId(), "토리", "social@test.com");

        // when & then
        assertNicknameConflict(() -> registerOAuth.register(command));
        assertThat(memberRepository.count()).isEqualTo(before);
        assertThat(memberRepository.findByEmail("social@test.com")).isEmpty();
    }

    @Test
    @DisplayName("다른 학교의 동일 닉네임은 새 가입에 사용할 수 있다")
    void 다른_학교는_허용한다() {
        // given
        School first = schools.학교("첫대학교");
        School second = schools.학교("둘대학교");
        saveMemberPort.save(Member.create("기존회원", "토리", "old@test.com", first.getId(), null));

        // when
        Long id = registerEmail.register(email(second.getId(), "토리", "new@test.com"));

        // then
        assertThat(memberRepository.findById(id)).get().extracting(Member::getNickname).isEqualTo("토리");
    }

    @Test
    @DisplayName("서비스를 직접 호출해도 한글 네 자를 초과하는 신규 닉네임은 거절한다")
    void 가입_서비스에서도_형식을_검증한다() {
        // given
        School school = schools.학교("형식대학교");
        var emailCommand = email(school.getId(), "가나다라마", "email@test.com");
        var oauthCommand = oauth(school.getId(), "가나다라마", "social@test.com");
        long before = memberRepository.count();

        // when & then
        assertThatThrownBy(() -> registerEmail.register(emailCommand))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode.code").isEqualTo("MEMBER-0019");
        assertThatThrownBy(() -> registerOAuth.register(oauthCommand))
            .isInstanceOf(MemberDomainException.class)
            .extracting("baseCode.code").isEqualTo("MEMBER-0019");
        assertThat(memberRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("기존의 중복·네 자 초과 닉네임을 변경하지 않는다")
    void 기존_닉네임을_유지한다() {
        // given
        School school = schools.학교("기존대학교");
        Member first = saveMemberPort.save(
            Member.create("첫회원", "기존긴닉네임", "first@test.com", school.getId(), null));
        Member second = saveMemberPort.save(
            Member.create("둘회원", "기존긴닉네임", "second@test.com", school.getId(), null));

        // when
        registerEmail.register(email(school.getId(), "새닉네임", "new@test.com"));

        // then
        assertThat(memberRepository.findAllById(List.of(first.getId(), second.getId())))
            .extracting(Member::getNickname).containsExactly("기존긴닉네임", "기존긴닉네임");
    }

    @Test
    @DisplayName("기존 복수 중복을 유지하면서 추가 중복 가입은 막는다")
    void 기존_복수_중복을_늘리지_않는다() {
        // given
        School school = schools.학교("중복대학교");
        saveMemberPort.save(Member.create("첫회원", "토리", "first@test.com", school.getId(), null));
        saveMemberPort.save(Member.create("둘회원", "토리", "second@test.com", school.getId(), null));
        long before = memberRepository.count();

        // when & then
        assertNicknameConflict(() -> registerEmail.register(email(school.getId(), "토리", "new@test.com")));
        assertThat(memberRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("수료 후 물리 삭제된 회원의 닉네임도 예약해 재사용을 차단한다")
    void 수료_탈퇴_후에도_닉네임을_보존한다() {
        // given
        School school = schools.학교("보존대학교");
        Member member = saveMemberPort.save(
            Member.create("수료회원", "토리", "graduated@test.com", school.getId(), null));
        var gisu = gisus.비활성_기수(10L);
        var challenger = challengers.웹(member.getId(), gisu.getId());
        challenger.changeStatus(ChallengerStatus.GRADUATED, null, "수료 테스트");
        saveChallengerPort.save(challenger);

        // when
        manageMember.deleteMember(DeleteMemberCommand.builder().memberId(member.getId()).build());

        // then
        assertThat(memberRepository.findById(member.getId())).isEmpty();
        assertThat(availability.check(school.getId(), "토리").available()).isFalse();
        assertNicknameConflict(() -> registerEmail.register(email(school.getId(), "토리", "new@test.com")));
    }

    @Test
    @DisplayName("미수료 회원의 탈퇴 후에는 같은 학교에서 닉네임을 재사용할 수 있다")
    void 미수료_탈퇴_후에는_재사용한다() {
        // given
        School school = schools.학교("재사용대학교");
        Member member = saveMemberPort.save(
            Member.create("미수료회원", "토리", "withdrawn@test.com", school.getId(), null));

        // when
        manageMember.deleteMember(DeleteMemberCommand.builder().memberId(member.getId()).build());
        Long newId = registerEmail.register(email(school.getId(), "토리", "new@test.com"));

        // then
        assertThat(newId).isNotEqualTo(member.getId());
        assertThat(memberRepository.findById(newId)).isPresent();
    }

    @Test
    @DisplayName("이메일 배치 안의 중복 닉네임은 전체 가입을 롤백한다")
    void 이메일_배치_중복을_차단한다() {
        // given
        School school = schools.학교("배치대학교");
        var commands = List.of(email(school.getId(), "토리", "one@test.com"),
            email(school.getId(), "토리", "two@test.com"));
        long before = memberRepository.count();

        // when & then
        assertNicknameConflict(() -> registerEmail.batchRegister(commands));
        assertThat(memberRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("OAuth 배치 안의 중복 닉네임도 전체 가입을 거절한다")
    void 소셜_배치_중복을_차단한다() {
        // given
        School school = schools.학교("소셜배치대학교");
        var commands = List.of(oauth(school.getId(), "토리", "one@test.com"),
            oauth(school.getId(), "토리", "two@test.com"));
        long before = memberRepository.count();

        // when & then
        assertNicknameConflict(() -> registerOAuth.batchRegister(commands));
        assertThat(memberRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("같은 학교·닉네임으로 이메일과 OAuth가 동시에 가입해도 한 명만 성공한다")
    void 동시_가입을_차단한다() throws Exception {
        // given
        School school = schools.학교("동시대학교");
        var emailCommand = email(school.getId(), "토리", "email@test.com");
        var oauthCommand = oauth(school.getId(), "토리", "social@test.com");
        var start = new CountDownLatch(1);
        var ready = new CountDownLatch(2);
        var executor = Executors.newFixedThreadPool(2);
        long before = memberRepository.count();
        try {
            var first = CompletableFuture.supplyAsync(() -> outcome(ready, start,
                () -> registerEmail.register(emailCommand)), executor);
            var second = CompletableFuture.supplyAsync(() -> outcome(ready, start,
                () -> registerOAuth.register(oauthCommand)), executor);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();

            // when
            start.countDown();
            var results = List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));

            // then
            assertThat(results).containsExactlyInAnyOrder("created", "duplicate");
            assertThat(memberRepository.count()).isEqualTo(before + 1);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private String outcome(CountDownLatch ready, CountDownLatch start, Runnable registration) {
        ready.countDown();
        try {
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시 가입 시작 대기 시간 초과");
            }
            registration.run();
            return "created";
        } catch (MemberDomainException exception) {
            if ("MEMBER-0020".equals(exception.getBaseCode().getCode())) {
                return "duplicate";
            }
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private void assertNicknameConflict(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action).isInstanceOf(MemberDomainException.class)
            .extracting("baseCode.code").isEqualTo("MEMBER-0020");
    }

    private EmailRegisterMemberCommand email(Long schoolId, String nickname, String email) {
        return EmailRegisterMemberCommand.builder().name("신규회원").nickname(nickname).email(email)
            .schoolId(schoolId).rawPassword("Password123!").termConsents(consents()).build();
    }

    private OAuthRegisterMemberCommand oauth(Long schoolId, String nickname, String email) {
        return OAuthRegisterMemberCommand.builder().name("신규회원").nickname(nickname).email(email)
            .schoolId(schoolId).provider(OAuthProvider.GOOGLE).providerId("provider-" + email)
            .termConsents(consents()).build();
    }

    private List<TermConsents> consents() {
        return getTermUseCase.getRequiredTermIds().stream()
            .map(id -> TermConsents.builder().termId(id).isAgreed(true).build()).toList();
    }
}
