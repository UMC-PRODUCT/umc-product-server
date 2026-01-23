package com.umc.product.storage.adapter.out.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GCS + Cloud CDN 기반 스토리지 어댑터
 *
 * <p>업로드는 GCS Signed URL을 사용하고,
 * 다운로드는 Cloud CDN Signed URL을 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GcsStorageAdapter implements StoragePort {

    private final Storage storage;
    private final GcsStorageProperties properties;

    @Override
    public FileUploadInfo generateUploadUrl(
            String storageKey,
            String contentType,
            long durationMinutes
    ) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(properties.bucketName(), storageKey)
                    .setContentType(contentType)
                    .build();

            URL signedUrl = storage.signUrl(
                    blobInfo,
                    durationMinutes,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                    Storage.SignUrlOption.withContentType()
            );

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", contentType);

            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(durationMinutes);

            log.debug("GCS 업로드 URL 생성: storageKey={}", storageKey);

            return new FileUploadInfo(
                    null,  // fileId는 Service에서 설정
                    signedUrl.toString(),
                    "PUT",
                    headers,
                    expiresAt
            );
        } catch (Exception e) {
            log.error("GCS 업로드 URL 생성 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_URL_GENERATION_FAILED);
        }
    }

    @Override
    public String generateAccessUrl(String storageKey, long durationMinutes) {
        GcsStorageProperties.Cdn cdn = properties.cdn();

        if (cdn != null && cdn.enabled()) {
            return generateCdnSignedUrl(storageKey, durationMinutes);
        }

        // CDN 미사용 시 GCS Signed URL 반환
        return generateGcsSignedUrl(storageKey, durationMinutes);
    }

    @Override
    public boolean exists(String storageKey) {
        BlobId blobId = BlobId.of(properties.bucketName(), storageKey);
        return storage.get(blobId) != null;
    }

    @Override
    public void delete(String storageKey) {
        try {
            BlobId blobId = BlobId.of(properties.bucketName(), storageKey);
            boolean deleted = storage.delete(blobId);

            if (!deleted) {
                log.warn("삭제할 파일이 존재하지 않음: storageKey={}", storageKey);
            } else {
                log.info("GCS 파일 삭제 완료: storageKey={}", storageKey);
            }
        } catch (Exception e) {
            log.error("GCS 파일 삭제 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_DELETE_FAILED);
        }
    }

    private String generateGcsSignedUrl(String storageKey, long durationMinutes) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(properties.bucketName(), storageKey).build();

            URL signedUrl = storage.signUrl(
                    blobInfo,
                    durationMinutes,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET)
            );

            return signedUrl.toString();
        } catch (Exception e) {
            log.error("GCS 다운로드 URL 생성 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.STORAGE_URL_GENERATION_FAILED);
        }
    }

    /**
     * Cloud CDN Signed URL 생성
     *
     * <p>Cloud CDN은 URL prefix 또는 개별 URL에 대해 서명을 지원합니다.
     * 여기서는 개별 URL 서명 방식을 사용합니다.
     *
     * @see <a href="https://cloud.google.com/cdn/docs/signed-urls">Cloud CDN Signed URLs</a>
     */
    private String generateCdnSignedUrl(String storageKey, long durationMinutes) {
        try {
            GcsStorageProperties.Cdn cdn = properties.cdn();

            // 만료 시간 (Unix timestamp)
            long expirationTime = Instant.now().plusSeconds(durationMinutes * 60).getEpochSecond();

            // URL 구성: {CDN_BASE_URL}/{storageKey}
            String urlToSign = String.format("%s/%s", cdn.baseUrl(), storageKey);

            // Signed URL 포맷: {URL}?Expires={timestamp}&KeyName={keyName}&Signature={signature}
            String urlWithExpiry = String.format("%s?Expires=%d&KeyName=%s",
                    urlToSign, expirationTime, cdn.keyName());

            // 서명 생성
            String signature = signUrl(urlWithExpiry, cdn.privateKey());

            String signedUrl = String.format("%s&Signature=%s", urlWithExpiry, signature);

            log.debug("CDN Signed URL 생성: storageKey={}", storageKey);

            return signedUrl;
        } catch (Exception e) {
            log.error("CDN Signed URL 생성 실패: storageKey={}", storageKey, e);
            throw new StorageException(StorageErrorCode.CDN_SIGNING_FAILED);
        }
    }

    /**
     * Cloud CDN URL 서명
     *
     * <p>HMAC-SHA1을 사용하여 URL에 서명합니다.
     */
    private String signUrl(String urlToSign, String privateKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(privateKey.trim());

        // HMAC-SHA1 서명
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(decodedKey, "HmacSHA1");
        mac.init(secretKeySpec);

        byte[] signatureBytes = mac.doFinal(urlToSign.getBytes(StandardCharsets.UTF_8));

        // URL-safe Base64 인코딩
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }
}
