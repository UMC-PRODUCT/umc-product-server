package com.umc.product.notification.application.service;

import com.umc.product.notification.application.port.in.SendEmailUseCase;
import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import com.umc.product.notification.application.port.out.SendEmailPort;
import com.umc.product.notification.application.port.out.dto.EmailMessage;
import com.umc.product.notification.domain.exception.EmailDomainException;
import com.umc.product.notification.domain.exception.EmailErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailService implements SendEmailUseCase {

    private static final String VERIFICATION_TEMPLATE = "email/verification";
    private static final String SUBJECT_PREFIX = "이메일 인증 코드: ";

    private final TemplateEngine templateEngine;
    private final SendEmailPort sendEmailPort;

    @Async("emailTaskExecutor")
    @Override
    public void sendVerificationEmail(SendVerificationEmailCommand command) {
        String htmlContent = renderVerificationTemplate(command);
        EmailMessage message = new EmailMessage(
            command.to(),
            SUBJECT_PREFIX + command.verificationCode(),
            htmlContent
        );
        // 발송 실패 시 SesEmailAdapter 가 to/awsErrorCode 컨텍스트와 cause 를 포함해 도메인 예외로 변환한다.
        // 비동기 호출이므로 예외는 AsyncUncaughtExceptionHandler 에서 처리된다.
        sendEmailPort.send(message);
    }

    private String renderVerificationTemplate(SendVerificationEmailCommand command) {
        try {
            Context context = new Context();
            context.setVariable("verificationToken", command.verificationCode());
            return templateEngine.process(VERIFICATION_TEMPLATE, context);
        } catch (RuntimeException e) {
            // 예외 삼킴 방지: 비동기 컨텍스트에서도 원인 추적이 가능하도록 stacktrace 와 컨텍스트를 로그에 남긴다.
            log.error("이메일 템플릿 렌더링 실패: to={}", command.to(), e);
            throw new EmailDomainException(EmailErrorCode.EMAIL_TEMPLATE_RENDER_FAILED, e);
        }
    }
}
