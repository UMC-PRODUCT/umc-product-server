package com.umc.product.notification.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.notification.application.port.in.SendEmailUseCase;
import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification/email")
@Profile("local | dev")
@RequiredArgsConstructor
public class EmailController {

    private final SendEmailUseCase sendEmailUseCase;

    @PostMapping("/send-test")
    @Public
    void sendTestEmail(
            @RequestBody SendVerificationEmailCommand command
    ) throws MessagingException, UnsupportedEncodingException {
        sendEmailUseCase.sendVerificationEmail(command);
    }
}
