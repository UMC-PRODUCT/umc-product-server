package com.umc.product.storage.adapter.out.s3;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.umc.product.global.logging.OperationalMetrics;
import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.application.port.out.dto.StorageObjectInfo;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * AWS S3 기반 스토리지 어댑터
 *
 * <p>업로드와 다운로드 모두 S3 Presigned URL을 사용합니다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
@RequiredArgsConstructor
public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3StorageProperties properties;
    private final OperationalMetrics operationalMetrics;

    @Override
    public FileUploadInfo generateUploadUrl(
        String storageKey,
        String contentType,
        long durationMinutes
    ) {
        return generateUploadUrl(storageKey, contentType, null, durationMinutes);
    }

    @Override
    public FileUploadInfo generateUploadUrl(
        String storageKey,
        String contentType,
        long fileSize,
        long durationMinutes
    ) {
        return generateUploadUrl(storageKey, contentType, Long.valueOf(fileSize), durationMinutes);
    }

    private FileUploadInfo generateUploadUrl(
        String storageKey,
        String contentType,
        Long fileSize,
        long durationMinutes
    ) {
        long startNanos = System.nanoTime();
        try {
            PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .contentType(contentType);

            if (fileSize != null) {
                putObjectRequestBuilder.contentLength(fileSize);
            }

            PutObjectRequest putObjectRequest = putObjectRequestBuilder.build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(durationMinutes))
                .putObjectRequest(putObjectRequest)
                .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            URL signedUrl = presignedRequest.url();

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", contentType);

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(durationMinutes);

            log.debug("S3 업로드 URL 생성: storageKey={}", storageKey);
            recordStorageMetric("CREATE_UPLOAD_URL", "success", startNanos);

            return new FileUploadInfo(
                null,  // fileId는 Service에서 설정
                signedUrl.toString(),
                "PUT",
                headers,
                expiresAt
            );
        } catch (Exception e) {
            recordStorageMetric("CREATE_UPLOAD_URL", "failure", startNanos);
            log.error("S3 업로드 URL 생성 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_URL_GENERATION_FAILED, e);
        }
    }

    @Override
    public String generateAccessUrl(String storageKey, long durationMinutes) {
        return generateS3DownloadUrl(storageKey, durationMinutes);
    }

    @Override
    public void uploadObject(String storageKey, String contentType, byte[] content) {
        long startNanos = System.nanoTime();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .contentType(contentType)
                .contentLength((long) content.length)
                .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
            recordStorageMetric("UPLOAD_OBJECT", "success", startNanos);
        } catch (Exception e) {
            recordStorageMetric("UPLOAD_OBJECT", "failure", startNanos);
            log.error("S3 객체 저장 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_UPLOAD_FAILED, e);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        return findObjectInfoByStorageKey(storageKey).isPresent();
    }

    @Override
    public Optional<StorageObjectInfo> findObjectInfoByStorageKey(String storageKey) {
        long startNanos = System.nanoTime();
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .build();
            HeadObjectResponse response = s3Client.headObject(headRequest);
            recordStorageMetric("GET_OBJECT_METADATA", "success", startNanos);
            return Optional.of(StorageObjectInfo.of(
                storageKey,
                response.contentLength(),
                response.contentType()
            ));
        } catch (NoSuchKeyException e) {
            recordStorageMetric("GET_OBJECT_METADATA", "not_found", startNanos);
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                recordStorageMetric("GET_OBJECT_METADATA", "not_found", startNanos);
                return Optional.empty();
            }
            recordStorageMetric("GET_OBJECT_METADATA", "failure", startNanos);
            log.error("S3 객체 정보 조회 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_METADATA_READ_FAILED, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        long startNanos = System.nanoTime();
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .build();

            s3Client.deleteObject(deleteRequest);
            log.info("S3 파일을 삭제했습니다: storageKey={}", storageKey);
            recordStorageMetric("DELETE_OBJECT", "success", startNanos);
        } catch (Exception e) {
            recordStorageMetric("DELETE_OBJECT", "failure", startNanos);
            log.error("S3 파일 삭제 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_DELETE_FAILED, e);
        }
    }

    // ==================== PRIVATE METHODS ====================

    private String generateS3DownloadUrl(String storageKey, long durationMinutes) {
        long startNanos = System.nanoTime();
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(durationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();
            String url = s3Presigner.presignGetObject(presignRequest).url().toString();
            recordStorageMetric("CREATE_DOWNLOAD_URL", "success", startNanos);
            return url;
        } catch (Exception e) {
            recordStorageMetric("CREATE_DOWNLOAD_URL", "failure", startNanos);
            log.error("S3 다운로드 URL 생성 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_URL_GENERATION_FAILED, e);
        }
    }

    private void recordStorageMetric(String operation, String result, long startNanos) {
        operationalMetrics.recordExternalCall(
            "STORAGE",
            operation,
            result,
            Duration.ofNanos(System.nanoTime() - startNanos)
        );
    }
}
