package com.umc.product.notification.adapter.out.external.ses;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS SES v2 발송 설정.
 *
 * <p>{@code accessKeyId}/{@code secretAccessKey}가 비어 있으면 DefaultCredentialsProvider 체인
 * (EC2 Instance Profile / IRSA / 환경변수 등)으로 폴백한다. S3 자격증명 해석 패턴과 동일하다.
 *
 * <p>{@code configurationSet}은 운영 환경에서 바운스/컴플레인트 트래킹을 활성화할 때만 주입한다.
 * 미설정 시 {@link SesEmailAdapter}는 SendEmail 요청에 해당 필드를 붙이지 않는다.
 */
@ConfigurationProperties(prefix = "notification.email.ses")
public record SesProperties(
    String region,
    String accessKeyId,
    String secretAccessKey,
    String fromAddress,
    String fromDisplayName,
    String configurationSet
) {

    public boolean hasStaticCredentials() {
        return accessKeyId != null && !accessKeyId.isBlank()
            && secretAccessKey != null && !secretAccessKey.isBlank();
    }

    public boolean hasConfigurationSet() {
        return configurationSet != null && !configurationSet.isBlank();
    }
}
