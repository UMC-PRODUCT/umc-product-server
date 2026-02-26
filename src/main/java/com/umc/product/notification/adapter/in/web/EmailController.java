package com.umc.product.notification.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.notification.application.port.in.SendEmailUseCase;
import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Notification | 이메일 전송 테스트", description = "개발 환경용 테스트 API")
public class EmailController {

    private final SendEmailUseCase sendEmailUseCase;

    @PostMapping("/send-test")
    @Public
    void sendTestEmail(
        @RequestBody SendVerificationEmailCommand command
    ) {
        sendEmailUseCase.sendVerificationEmail(command);
    }
}
