package com.umc.product.test.application.service;

import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.in.command.dto.IdPwRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.OAuthRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * datafaker 를 사용해 test 도메인 시딩용 더미 회원 Command 를 생성한다. ADR-017 참조.
 * <p>
 * V2026.02.28.06.00 마이그레이션이 school 1~38 을 시딩하므로 schoolId 무작위 추출은 그 범위에서 고른다.
 * loginId 는 sequence 번호로 고정해 멱등성과 유일성을 동시에 만족시킨다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class DummyMemberFactory {

    private static final long MIN_SCHOOL_ID = 1L;
    private static final long MAX_SCHOOL_ID = 38L;
    private static final int MAX_NICKNAME_LENGTH = 20;

    private final SeedProperties properties;
    private final GetTermUseCase getTermUseCase;

    private final Faker faker = new Faker(Locale.KOREAN);

    private static final String APPLE_DEFAULT_CLIENT_ID = "com.umc.product";
    private static final OAuthProvider[] PROVIDER_ROTATION = {
        OAuthProvider.GOOGLE,
        OAuthProvider.APPLE,
        OAuthProvider.KAKAO
    };

    /**
     * ID/PW 회원가입용 Command 를 만든다. loginId 는 alpha_user_0001 형식(CredentialPolicy 정규식 만족).
     */
    public IdPwRegisterMemberCommand nextIdPwCommand(int sequence) {
        String loginId = "alpha_user_%04d".formatted(sequence);
        String email = "%s@%s".formatted(loginId, properties.emailDomain());
        return IdPwRegisterMemberCommand.builder()
            .loginId(loginId)
            .rawPassword(properties.defaultPassword())
            .name(faker.name().fullName())
            .nickname(safeNickname(faker.name().firstName(), sequence))
            .email(email)
            .schoolId(randomSchoolId())
            .termConsents(allMandatoryConsents())
            .build();
    }

    /**
     * OAuth 회원가입용 Command 를 total 개 만든다. provider 는 GOOGLE → APPLE → KAKAO 라운드로빈으로
     * 분배되어 균등에 가까운 분포를 만든다. APPLE 은 appleRefreshToken 과 appleClientId 를 함께 채운다.
     * <p>
     * providerId 는 (provider, sequence) 조합으로 만들어 uk_member_oauth_provider_provider_id UNIQUE 제약을
     * 자연스럽게 만족시킨다.
     */
    public List<OAuthRegisterMemberCommand> nextOAuthCommands(int total) {
        List<OAuthRegisterMemberCommand> commands = new ArrayList<>(total);
        for (int seq = 1; seq <= total; seq++) {
            OAuthProvider provider = PROVIDER_ROTATION[(seq - 1) % PROVIDER_ROTATION.length];
            commands.add(buildOAuthCommand(seq, provider));
        }
        return commands;
    }

    private OAuthRegisterMemberCommand buildOAuthCommand(int sequence, OAuthProvider provider) {
        String providerTag = provider.name().toLowerCase(Locale.ROOT);
        String providerId = "alpha_%s_%04d".formatted(providerTag, sequence);
        String email = "alpha_oauth_%04d@%s".formatted(sequence, properties.emailDomain());
        boolean isApple = provider == OAuthProvider.APPLE;
        return OAuthRegisterMemberCommand.builder()
            .provider(provider)
            .providerId(providerId)
            .name(faker.name().fullName())
            .nickname(safeNickname(faker.name().firstName(), sequence))
            .email(email)
            .schoolId(randomSchoolId())
            .profileImageId(null)
            .termConsents(allMandatoryConsents())
            .appleRefreshToken(isApple ? "alpha_apple_refresh_%04d".formatted(sequence) : null)
            .appleClientId(isApple ? APPLE_DEFAULT_CLIENT_ID : null)
            .build();
    }

    /**
     * 현재 활성 필수 약관 전체에 대해 isAgreed=true 인 동의 목록을 만든다.
     * MemberRegistrationValidator.validateMandatoryTermsAgreed 를 통과하는 최소 조건.
     */
    private List<TermConsents> allMandatoryConsents() {
        Set<Long> requiredTermIds = getTermUseCase.getRequiredTermIds();
        return requiredTermIds.stream()
            .map(termId -> TermConsents.builder().termId(termId).isAgreed(true).build())
            .toList();
    }

    private long randomSchoolId() {
        return ThreadLocalRandom.current().nextLong(MIN_SCHOOL_ID, MAX_SCHOOL_ID + 1);
    }

    /**
     * Member.nickname 컬럼 길이 20 을 초과하지 않게 자르고, sequence 를 붙여 유일성을 보강한다.
     */
    private String safeNickname(String base, int sequence) {
        String candidate = base + sequence;
        if (candidate.length() > MAX_NICKNAME_LENGTH) {
            return candidate.substring(0, MAX_NICKNAME_LENGTH);
        }
        return candidate;
    }
}
