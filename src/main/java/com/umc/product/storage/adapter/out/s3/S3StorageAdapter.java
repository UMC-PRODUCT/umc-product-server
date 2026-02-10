package com.umc.product.storage.adapter.out.s3;

import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import java.io.StringReader;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * AWS S3 + CloudFront 기반 스토리지 어댑터
 *
 * <p>업로드는 S3 Presigned URL을 사용하고,
 * 다운로드는 CloudFront Signed URL을 사용합니다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
@RequiredArgsConstructor
public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3StorageProperties properties;

    @Value("${spring.profiles.active:default}")
    private String springProfile;

    @Override
    public FileUploadInfo generateUploadUrl(
        String storageKey,
        String contentType,
        long durationMinutes
    ) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .contentType(contentType)
                .build();

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

            return new FileUploadInfo(
                null,  // fileId는 Service에서 설정
                signedUrl.toString(),
                "PUT",
                headers,
                expiresAt
            );
        } catch (Exception e) {
            log.error("S3 업로드 URL 생성 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_URL_GENERATION_FAILED);
        }
    }

    @Override
    public String generateAccessUrl(String storageKey, long durationMinutes) {
        S3StorageProperties.CloudFront cloudfront = properties.cloudfront();

        if (cloudfront != null && cloudfront.enabled()) {
            return generateCloudFrontSignedUrl(storageKey, durationMinutes);
        }

        // CloudFront 미사용 시 S3 Presigned URL 반환
        return generateS3PresignedUrl(storageKey, durationMinutes);
    }

    @Override
    public boolean exists(String storageKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .build();

            s3Client.deleteObject(deleteRequest);
            log.info("S3 파일 삭제 완료: storageKey={}", storageKey);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_DELETE_FAILED);
        }
    }

    private String generateS3PresignedUrl(String storageKey, long durationMinutes) {
        try {
            var getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey)
                .build();

            var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(durationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

            var presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("S3 다운로드 URL 생성 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_URL_GENERATION_FAILED);
        }
    }

    /**
     * CloudFront Signed URL 생성
     *
     * @see <a
     * href="https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-signed-urls.html">CloudFront
     * Signed URLs</a>
     */
    private String generateCloudFrontSignedUrl(String storageKey, long durationMinutes) {
        try {
            S3StorageProperties.CloudFront cloudfront = properties.cloudfront();

            // 만료 시간
            Instant expirationTime = Instant.now().plusSeconds(durationMinutes * 60);

            // URL 인코딩 (특수문자 처리)
            String encodedKey = encodeStorageKey(storageKey);

            // URL 구성: https://{distribution-domain}/{storageKey}
            String resourceUrl = String.format("https://%s/%s/%s",
                cloudfront.distributionDomain(),
                parseSpringProfileToCloudFrontPath(),
                encodedKey);

            // CloudFront 유틸리티를 사용하여 서명
            CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

            // Private Key 파싱
            PrivateKey privateKey = parsePrivateKey(cloudfront.privateKey());

            CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                .resourceUrl(resourceUrl)
                .privateKey(privateKey)
                .keyPairId(cloudfront.keyPairId())
                .expirationDate(expirationTime)
                .build();

            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(signerRequest);

            log.debug("CloudFront Signed URL 생성 완료: storageKey={}, url={}", storageKey, resourceUrl);

            return signedUrl.url();
        } catch (Exception e) {
            log.error("CloudFront Signed URL 생성 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.CDN_SIGNING_FAILED);
        }
    }

    /**
     * PEM 형식의 Private Key를 파싱합니다.
     */
    private PrivateKey parsePrivateKey(String privateKeyPem) throws Exception {
        // PEM 형식인지 확인
        if (privateKeyPem.contains("-----BEGIN")) {
            try (PemReader pemReader = new PemReader(new StringReader(privateKeyPem))) {
                PemObject pemObject = pemReader.readPemObject();
                byte[] keyBytes = pemObject.getContent();

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                return keyFactory.generatePrivate(keySpec);
            }
        }

        // Base64로 인코딩된 DER 형식
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    private String encodeStorageKey(String storageKey) {
        // 이미 인코딩되어 있지 않은 경우에만 인코딩
        // CloudFront는 path 부분은 그대로 두고 쿼리 파라미터만 인코딩해야 함
        return storageKey; // 또는 필요시 URLEncoder.encode() 사용
    }

    private String parseSpringProfileToCloudFrontPath() {
        return switch (springProfile) {
            case "prod" -> "prod";
            case "dev" -> "dev";
            case "local" -> "local";
            default -> throw new StorageException(StorageErrorCode.INVALID_SPRING_PROFILE);
        };
    }
}
