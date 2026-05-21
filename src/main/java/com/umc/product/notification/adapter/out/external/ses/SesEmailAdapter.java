package com.umc.product.notification.adapter.out.external.ses;

import com.umc.product.notification.application.port.out.SendEmailPort;
import com.umc.product.notification.application.port.out.dto.EmailMessage;
import com.umc.product.notification.domain.exception.EmailDomainException;
import com.umc.product.notification.domain.exception.EmailErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

/**
 * AWS SES v2 기반 이메일 발송 어댑터.
 *
 * <p>{@link SendEmailPort} 의 인프라 측 구현. application 레이어가 SDK 타입에 노출되지 않도록 격리한다.
 * Configuration Set 은 {@link SesProperties#hasConfigurationSet()} 일 때만 요청에 부착한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SesEmailAdapter implements SendEmailPort {

    private static final String CHARSET_UTF_8 = "UTF-8";

    private final SesV2Client sesV2Client;
    private final SesProperties properties;

    @Override
    public void send(EmailMessage message) {
        SendEmailRequest request = buildRequest(message);
        try {
            sesV2Client.sendEmail(request);
        } catch (SesV2Exception e) {
            // 예외 삼킴 방지: AWS error code 까지 컨텍스트에 남기고 cause 를 포함해 도메인 예외로 변환한다.
            String awsErrorCode = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null;
            log.error("SES 발송 실패: to={}, awsErrorCode={}", message.to(), awsErrorCode, e);
            throw new EmailDomainException(EmailErrorCode.EMAIL_SEND_FAILED, e);
        } catch (RuntimeException e) {
            log.error("SES 발송 중 예기치 못한 예외: to={}", message.to(), e);
            throw new EmailDomainException(EmailErrorCode.EMAIL_SEND_FAILED, e);
        }
    }

    private SendEmailRequest buildRequest(EmailMessage message) {
        Content subject = Content.builder().charset(CHARSET_UTF_8).data(message.subject()).build();
        Content htmlBody = Content.builder().charset(CHARSET_UTF_8).data(message.htmlBody()).build();

        Message sesMessage = Message.builder()
            .subject(subject)
            .body(Body.builder().html(htmlBody).build())
            .build();

        SendEmailRequest.Builder builder = SendEmailRequest.builder()
            .fromEmailAddress(formatFromAddress())
            .destination(Destination.builder().toAddresses(message.to()).build())
            .content(EmailContent.builder().simple(sesMessage).build());

        if (properties.hasConfigurationSet()) {
            builder.configurationSetName(properties.configurationSet());
        }

        return builder.build();
    }

    /**
     * RFC 5322 형식의 발신 주소 문자열을 구성한다. (예: {@code "University MakeUs Challenge" <noreply@umc.it.kr>})
     */
    private String formatFromAddress() {
        return String.format("\"%s\" <%s>", properties.fromDisplayName(), properties.fromAddress());
    }
}
