package com.umc.product.global.seed;

import com.umc.product.member.application.port.in.command.dto.IdPwRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

/**
 * datafaker 를 사용해 alpha 환경용 더미 회원 Command 를 생성한다. ADR-007 참조.
 * <p>
 * V2026.02.28.06.00 마이그레이션이 school 1~38 을 시딩하므로 schoolId 는 그 범위에서 무작위로 고른다.
 * loginId 는 sequence 번호로 고정해 멱등성과 유일성을 동시에 만족시킨다.
 */
@Component
@RequiredArgsConstructor
public class AlphaDummyMemberFactory {

    private static final long MIN_SCHOOL_ID = 1L;
    private static final long MAX_SCHOOL_ID = 38L;
    private static final int MAX_NICKNAME_LENGTH = 20;

    private final AlphaSeedProperties properties;
    private final GetTermUseCase getTermUseCase;

    private final Faker faker = new Faker(Locale.KOREAN);

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
