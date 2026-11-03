package com.umc.product.notification.adapter.out.external.log;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.umc.product.notification.application.port.out.SendEmailPort;
import com.umc.product.notification.application.port.out.dto.EmailMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * 메일을 실제로 보내지 않고 로그로만 남기는 로컬 개발용 어댑터.
 *
 * <p>SES·SMTP 자격증명 없이 서버를 띄우고, 로컬에서 실수로 실제 메일이 나가지 않게 한다.
 * prod 에서는 등록하지 않으므로 prod 에서 {@code log}를 고르면 발송 포트가 없어 기동이 실패한다.
 * 수신 주소와 본문(인증 코드)은 로그에 남기지 않는다.
 */
@Slf4j
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.notification.email", name = "provider", havingValue = "log")
public class LogEmailAdapter implements SendEmailPort {

    @Override
    public void send(EmailMessage message) {
        log.info("이메일을 발송하지 않고 로그로 대신합니다: {} {} {}",
            kv("provider", "LOG"), kv("recipientPresent", message.to() != null && !message.to().isBlank()),
            kv("subject", message.subject()));
    }
}
