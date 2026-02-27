package com.umc.product.storage.adapter.out.gcs;

import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GCS 및 Cloud CDN 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "storage.gcs")
public record GcsStorageProperties(
        String bucketName,
        String projectId,
        String credentialsJson,
        Cdn cdn
) {
    public record Cdn(
            boolean enabled,
            String baseUrl,
            String keyName,
            String privateKey
    ) {
        public Cdn {
            if (enabled && (baseUrl == null || keyName == null || privateKey == null)) {
                throw new StorageException(StorageErrorCode.NO_ENV_KEYS);
            }
        }
    }
}
