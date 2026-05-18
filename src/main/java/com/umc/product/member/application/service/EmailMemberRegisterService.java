package com.umc.product.member.application.service;

import com.umc.product.audit.domain.AuditAction;
import com.umc.product.audit.domain.AuditLogEvent;
import com.umc.product.authentication.application.port.in.command.CredentialAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialByEmailCommand;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.command.RegisterEmailMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 이메일 기반 회원가입 서비스. ADR-017 흐름.
 * <p>
 * loginId 는 받지 않으며, 이메일은 emailVerificationToken 에서 추출되어 이미 Command 에 포함되어 있다.
 */
@Service
@RequiredArgsConstructor
public class EmailMemberRegisterService implements RegisterEmailMemberUseCase {

    private final SaveMemberPort saveMemberPort;

    private final MemberRegistrationValidator registrationValidator;

    private final CredentialAuthenticationUseCase credentialAuthenticationUseCase;
    private final GetSchoolUseCase getSchoolUseCase;

    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public Long register(EmailRegisterMemberCommand command) {
        registrationValidator.validateSchoolExists(command.schoolId());
        registrationValidator.validateMandatoryTermsAgreed(command.termConsents());

        Member created = saveMemberPort.save(command.toEntity());

        credentialAuthenticationUseCase.registerCredentialByEmail(
            RegisterCredentialByEmailCommand.of(created.getId(), command.rawPassword())
        );

        String logDescription = getSchoolUseCase.getSchoolDetail(command.schoolId()).schoolName()
            + "소속 " + command.nickname() + "/" + command.name()
            + " 님이 회원 가입하셨습니다.";

        eventPublisher.publish(
            AuditLogEvent.builder()
                .domain(Domain.MEMBER)
                .action(AuditAction.REGISTER)
                .targetType("Member")
                .targetId(String.valueOf(created.getId()))
                .description(logDescription)
                .build()
        );

        return created.getId();
    }
}
