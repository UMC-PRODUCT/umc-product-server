package com.umc.product.storage.adapter.out.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS S3 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "storage.s3")
public record S3StorageProperties(
    String bucketName,
    String region,
    String accessKeyId,
    String secretAccessKey
) {
}
