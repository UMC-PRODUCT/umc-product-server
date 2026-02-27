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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // fromAddress는 바꿔도 SMTP 설정대로 전송됨
            helper.setFrom(fromAddress, "University MakeUs Challenge");
            helper.setTo(command.to());
            helper.setSubject("University MakeUs Challenge: 이메일 인증 코드");

            // Thymeleaf 템플릿 렌더링
            Context context = new Context();
            context.setVariable("verificationToken", command.verificationCode());
//            context.setVariable("verificationLink", command.verificationLink());

            String htmlContent = templateEngine.process("email/verification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailDomainException(EmailErrorCode.EMAIL_MESSAGING_ERROR);
        } catch (UnsupportedEncodingException e) {
            throw new EmailDomainException(EmailErrorCode.EMAIL_ENCODING_ERROR);
        } catch (Exception e) {
            throw new EmailDomainException(EmailErrorCode.EMAIL_GENERAL_ERROR);
        }

    }
}
