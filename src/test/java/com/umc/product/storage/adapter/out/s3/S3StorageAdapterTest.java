package com.umc.product.storage.adapter.out.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.out.dto.StorageObjectInfo;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    S3Client s3Client;

    @Mock
    S3Presigner s3Presigner;

    @Mock
    PresignedPutObjectRequest presignedPutObjectRequest;

    @Test
    @DisplayName("Presigned PUT 생성 시 요청 파일 크기를 Content-Length로 서명한다")
    void Presigned_PUT_생성_시_요청_파일_크기를_Content_Length로_서명한다() throws Exception {
        // given
        S3StorageAdapter sut = new S3StorageAdapter(s3Client, s3Presigner, properties());
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        given(presignedPutObjectRequest.url()).willReturn(URI.create("https://storage.example.com/upload").toURL());
        given(s3Presigner.presignPutObject(captor.capture())).willReturn(presignedPutObjectRequest);

        // when
        FileUploadInfo result = sut.generateUploadUrl("private/portfolio/file.pdf", "application/pdf", 1024L, 15L);

        // then
        assertThat(result.uploadUrl()).isEqualTo("https://storage.example.com/upload");
        assertThat(result.uploadMethod()).isEqualTo("PUT");
        assertThat(result.headers()).containsEntry("Content-Type", "application/pdf");
        assertThat(result.headers()).doesNotContainKey("Content-Length");
        assertThat(result.expiresAt()).isAfter(LocalDateTime.now());
        assertThat(captor.getValue().putObjectRequest().contentLength()).isEqualTo(1024L);
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("HeadObject 결과를 S3 객체 정보로 반환한다")
    void HeadObject_결과를_S3_객체_정보로_반환한다() {
        // given
        S3StorageAdapter sut = new S3StorageAdapter(s3Client, s3Presigner, properties());
        given(s3Client.headObject(org.mockito.ArgumentMatchers.any(HeadObjectRequest.class)))
            .willReturn(HeadObjectResponse.builder()
                .contentLength(1024L)
                .contentType("application/pdf")
                .build());

        // when
        Optional<StorageObjectInfo> result = sut.findObjectInfoByStorageKey("private/portfolio/file.pdf");

        // then
        assertThat(result).hasValue(StorageObjectInfo.of("private/portfolio/file.pdf", 1024L, "application/pdf"));
    }

    @Test
    @DisplayName("S3 객체가 없으면 빈 객체 정보를 반환하고 exists는 false를 반환한다")
    void S3_객체가_없으면_빈_객체_정보를_반환하고_exists는_false를_반환한다() {
        // given
        S3StorageAdapter sut = new S3StorageAdapter(s3Client, s3Presigner, properties());
        given(s3Client.headObject(org.mockito.ArgumentMatchers.any(HeadObjectRequest.class)))
            .willThrow(NoSuchKeyException.builder().build());

        // when & then
        assertThat(sut.findObjectInfoByStorageKey("private/portfolio/missing.pdf")).isEmpty();
        assertThat(sut.exists("private/portfolio/missing.pdf")).isFalse();
    }

    @Test
    @DisplayName("HeadObject 조회 중 404가 아닌 S3Exception은 StorageException으로 변환한다")
    void HeadObject_조회_중_404가_아닌_S3Exception은_StorageException으로_변환한다() {
        // given
        S3StorageAdapter sut = new S3StorageAdapter(s3Client, s3Presigner, properties());
        given(s3Client.headObject(org.mockito.ArgumentMatchers.any(HeadObjectRequest.class)))
            .willThrow(S3Exception.builder().statusCode(500).build());

        // when & then
        assertThatThrownBy(() -> sut.findObjectInfoByStorageKey("private/portfolio/file.pdf"))
            .isInstanceOf(StorageException.class)
            .hasCauseInstanceOf(S3Exception.class)
            .extracting("baseCode")
            .isEqualTo(StorageErrorCode.STORAGE_METADATA_READ_FAILED);
    }

    private S3StorageProperties properties() {
        return new S3StorageProperties(
            "test-bucket",
            "ap-northeast-2",
            "access-key",
            "secret-key",
            new S3StorageProperties.CloudFront("cdn.example.com", false, null, null)
        );
    }
}
