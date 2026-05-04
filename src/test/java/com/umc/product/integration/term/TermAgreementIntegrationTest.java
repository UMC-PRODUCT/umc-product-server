package com.umc.product.integration.term;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.member.domain.Member;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.TermFixture;
import com.umc.product.term.application.port.in.command.ManageTermAgreementUseCase;
import com.umc.product.term.application.port.in.command.dto.CreateTermConsentCommand;
import com.umc.product.term.application.port.in.query.GetTermAgreementUseCase;
import com.umc.product.term.application.port.in.query.dto.TermInfo;
import com.umc.product.term.application.port.out.LoadTermConsentPort;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link IntegrationTestSupport} + Fixture 결합 사용 예시.
 *
 * <p>Member 도메인과 Term 도메인을 가로질러 호출하는 흐름을 검증한다.</p>
 * <ul>
 *   <li>Fixture 로 사전 데이터 적재 → UseCase 호출 → Port 로 영속 결과 검증의 표준 흐름</li>
 *   <li>성공 시나리오 + 도메인 예외 시나리오 양쪽 모두 포함</li>
 * </ul>
 */
class TermAgreementIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ManageTermAgreementUseCase manageTermAgreementUseCase;

    @Autowired
    private GetTermAgreementUseCase getTermAgreementUseCase;

    @Autowired
    private LoadTermConsentPort loadTermConsentPort;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private TermFixture termFixture;

    @Test
    void 회원이_약관에_동의하면_동의_정보가_저장되고_조회된다() {
        // given
        Member member = memberFixture.일반("길동");
        Term term = termFixture.필수_약관(TermType.SERVICE);

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(member.getId())
            .termId(term.getId())
            .isAgreed(true)
            .build();

        // when
        manageTermAgreementUseCase.createTermConsent(command);

        // then: Port 로 직접 조회해 영속 상태 검증
        assertThat(loadTermConsentPort.existsByMemberIdAndTermType(member.getId(), TermType.SERVICE))
            .isTrue();

        // and: Query UseCase 도 동일하게 동의 약관을 반환
        List<TermInfo> agreed = getTermAgreementUseCase.getAgreedTermsByMemberId(member.getId());
        assertThat(agreed)
            .extracting(TermInfo::type)
            .containsExactly(TermType.SERVICE);
    }

    @Test
    void 동일_약관에_중복_동의하면_TERMS_CONSENT_ALREADY_EXISTS_예외가_발생한다() {
        // given
        Member member = memberFixture.일반("이몽룡");
        Term term = termFixture.필수_약관(TermType.PRIVACY);
        termFixture.약관_동의(member.getId(), TermType.PRIVACY);

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(member.getId())
            .termId(term.getId())
            .isAgreed(true)
            .build();

        // when & then
        assertThatThrownBy(() -> manageTermAgreementUseCase.createTermConsent(command))
            .isInstanceOf(TermDomainException.class)
            .extracting("baseCode")
            .isEqualTo(TermErrorCode.TERMS_CONSENT_ALREADY_EXISTS);
    }

    @Test
    void 존재하지_않는_약관_ID로_동의를_시도하면_TERMS_NOT_FOUND_예외가_발생한다() {
        // given
        Member member = memberFixture.일반("성춘향");
        Long nonExistentTermId = 9_999L;

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(member.getId())
            .termId(nonExistentTermId)
            .isAgreed(true)
            .build();

        // when & then
        assertThatThrownBy(() -> manageTermAgreementUseCase.createTermConsent(command))
            .isInstanceOf(TermDomainException.class)
            .extracting("baseCode")
            .isEqualTo(TermErrorCode.TERMS_NOT_FOUND);
    }
}
