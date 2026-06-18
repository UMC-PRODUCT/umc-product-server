package com.umc.product.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.storage.domain.enums.StorageProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StorageProviderPolicyTest {

    @Test
    @DisplayName("스토리지 제공자는 AWS S3만 지원한다")
    void storage_provider_is_s3_only() throws IOException {
        assertThat(StorageProvider.values()).containsExactly(StorageProvider.AWS_S3);
        assertThat(Path.of("src/main/java/com/umc/product/storage/adapter/out/gcs")).doesNotExist();
        assertThat(Files.readString(Path.of("build.gradle.kts"))).doesNotContain("google-cloud-storage");
        assertThat(Files.readString(Path.of("src/main/resources/application.yml"))).doesNotContain("  gcs:");
        assertThat(Files.readString(Path.of(".env.example"))).doesNotContain("GCS_", "gcs");
    }
}
