package com.umc.product.storage.adapter.out.gcs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "storage.provider", havingValue = "gcs")
@EnableConfigurationProperties(GcsStorageProperties.class)
public class GcsConfig {

    @Bean
    public Storage googleCloudStorage(GcsStorageProperties properties) throws IOException {
        GoogleCredentials credentials = resolveCredentials(properties.credentialsJson());

        return StorageOptions.newBuilder()
                .setProjectId(properties.projectId())
                .setCredentials(credentials)
                .build()
                .getService();
    }

    private GoogleCredentials resolveCredentials(String credentialsJson) throws IOException {
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            return GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
            );
        }
        return GoogleCredentials.getApplicationDefault();
    }
}
