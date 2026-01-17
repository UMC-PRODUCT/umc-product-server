package com.umc.product.notification.application.port.in;

import com.umc.product.notification.application.port.in.dto.SendVerificationEmailCommand;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface SendEmailUseCase {
    void sendVerificationEmail(SendVerificationEmailCommand command)
            throws MessagingException, UnsupportedEncodingException;
}
