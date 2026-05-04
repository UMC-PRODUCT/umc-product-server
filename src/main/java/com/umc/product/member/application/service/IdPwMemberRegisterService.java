package com.umc.product.member.application.service;

import com.umc.product.audit.domain.AuditAction;
import com.umc.product.audit.domain.AuditLogEvent;
import com.umc.product.authentication.application.port.in.command.CredentialAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.RegisterCredentialCommand;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.command.RegisterIdPwMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.IdPwRegisterMemberCommand;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdPwMemberRegisterService implements RegisterIdPwMemberUseCase {

    private final SaveMemberPort saveMemberPort;

    private final MemberRegistrationValidator registrationValidator;

    private final CredentialAuthenticationUseCase credentialAuthenticationUseCase;
    private final GetSchoolUseCase getSchoolUseCase;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Long register(IdPwRegisterMemberCommand command) {
        registrationValidator.validateSchoolExists(command.schoolId());
        registrationValidator.validateMandatoryTermsAgreed(command.termConsents());

        Member created = saveMemberPort.save(command.toEntity());

        credentialAuthenticationUseCase.registerCredential(
            RegisterCredentialCommand.of(
                created.getId(), command.loginId(), command.rawPassword()
            )
        );

        String logDescription = getSchoolUseCase.getSchoolDetail(command.schoolId()).schoolName()
            + "소속 " + command.nickname() + "/" + command.name() +
            " 님이 회원 가입하셨습니다.";

        eventPublisher.publishEvent(
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
