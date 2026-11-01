package com.umc.product.notification.adapter.out.external.log;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.umc.product.notification.application.port.out.dto.EmailMessage;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class LogEmailAdapterTest {

    @Test
    @DisplayName("LOG 제공자는 메일을 보내지 않고 수신자 존재 여부와 제목만 로그에 남긴다")
    void logs_recipient_presence_and_subject_without_address_or_body() {
        // given
        Logger logger = (Logger) LoggerFactory.getLogger(LogEmailAdapter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);
        EmailMessage message = new EmailMessage(
            "no-reply@umc.it.kr", "UMC", "university@example.org", "이메일 인증 코드", "인증 코드: 123456", false
        );

        try {
            // when
            new LogEmailAdapter().send(message);

            // then
            assertThat(appender.list).hasSize(1);
            assertThat(appender.list.get(0).getFormattedMessage())
                .contains("recipientPresent=true", "이메일 인증 코드")
                .doesNotContain("university", "example.org", "123456");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
            logger.setLevel(previousLevel);
        }
    }
}
