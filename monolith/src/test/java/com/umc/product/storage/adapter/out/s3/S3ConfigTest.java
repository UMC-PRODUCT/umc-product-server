package com.umc.product.storage.adapter.out.s3;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

class S3ConfigTest {

    @Test
    @DisplayName("S3 정적 자격증명이 있으면 그 값을 사용한다")
    void S3_정적_자격증명이_있으면_그_값을_사용한다() {
        // given
        S3Config sut = new S3Config();
        S3StorageProperties storageProperties = storageProperties("s3-access-key", "s3-secret-key");

        // when
        AwsCredentialsProvider credentialsProvider = sut.resolveS3Credentials(storageProperties);

        // then
        assertThat(credentialsProvider.resolveCredentials().accessKeyId()).isEqualTo("s3-access-key");
        assertThat(credentialsProvider.resolveCredentials().secretAccessKey()).isEqualTo("s3-secret-key");
    }

    @Test
    @DisplayName("S3 정적 자격증명이 없으면 기본 자격증명 체인을 사용한다")
    void S3_정적_자격증명이_없으면_기본_자격증명_체인을_사용한다() {
        // given
        S3Config sut = new S3Config();
        S3StorageProperties storageProperties = storageProperties("", "");

        // when
        AwsCredentialsProvider credentialsProvider = sut.resolveS3Credentials(storageProperties);

        // then
        assertThat(credentialsProvider).isInstanceOf(DefaultCredentialsProvider.class);
    }

    private S3StorageProperties storageProperties(String accessKeyId, String secretAccessKey) {
        return new S3StorageProperties("test-bucket", "ap-northeast-2", accessKeyId, secretAccessKey);
    }
}
