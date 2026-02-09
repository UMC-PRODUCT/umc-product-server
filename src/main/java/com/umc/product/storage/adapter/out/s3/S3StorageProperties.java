package com.umc.product.storage.adapter.out.s3;

import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS S3 및 CloudFront 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "storage.s3")
public record S3StorageProperties(
    String bucketName,
    String region,
    String accessKeyId,
    String secretAccessKey,
    CloudFront cloudfront
) {
    public record CloudFront(
        boolean enabled,
        String distributionDomain,
        String keyPairId,
        String privateKey
    ) {
        public CloudFront {
            if (enabled && (distributionDomain == null || keyPairId == null || privateKey == null)) {
                throw new StorageException(StorageErrorCode.NO_ENV_KEYS);
            }
        }
    }
}
