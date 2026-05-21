package com.umc.product.notification.application.service;

import com.umc.product.notification.application.port.in.SendEmailUseCase;
import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import com.umc.product.notification.domain.exception.EmailDomainException;
import com.umc.product.notification.domain.exception.EmailErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailService implements SendEmailUseCase {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async("emailTaskExecutor")
    @Override
    public void sendVerificationEmail(SendVerificationEmailCommand command) {
        String htmlContent = renderVerificationTemplate(command);
        dispatch(command, htmlContent);
    }

    private String renderVerificationTemplate(SendVerificationEmailCommand command) {
        try {
            Context context = new Context();
            context.setVariable("verificationToken", command.verificationCode());
            return templateEngine.process("email/verification", context);
        } catch (RuntimeException e) {
            // 예외 삼킴 방지: 비동기 컨텍스트에서도 원인 추적이 가능하도록 stacktrace 와 컨텍스트를 로그에 남긴다.
            log.error("이메일 템플릿 렌더링 실패: to={}", command.to(), e);
            throw new EmailDomainException(EmailErrorCode.EMAIL_TEMPLATE_RENDER_FAILED, e);
        }
    }

    private void dispatch(SendVerificationEmailCommand command, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // fromAddress는 바꿔도 SMTP 설정대로 전송됨
            helper.setFrom(fromAddress, "University MakeUs Challenge");
            helper.setTo(command.to());
            helper.setSubject("이메일 인증 코드: " + command.verificationCode());
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("이메일 발송 실패(SMTP): to={}", command.to(), e);
            throw new EmailDomainException(EmailErrorCode.EMAIL_SEND_FAILED, e);
        } catch (RuntimeException e) {
            log.error("이메일 발송 중 예기치 못한 예외: to={}", command.to(), e);
            throw new EmailDomainException(EmailErrorCode.EMAIL_SEND_FAILED, e);
        }
    }
}
