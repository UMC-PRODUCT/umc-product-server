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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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
     * @see <a href="https://cloud.google.com/cdn/docs/signed-urls">Cloud CDN Signed URLs</a>
     */
    private String generateCdnSignedUrl(String storageKey, long durationMinutes) {
        try {
            GcsStorageProperties.Cdn cdn = properties.cdn();

            // 만료 시간 (Unix timestamp)
            long expirationTime = Instant.now().plusSeconds(durationMinutes * 60).getEpochSecond();

            log.info("durationMinutes: {}", durationMinutes);
            log.info("calculated expiration: {}", Instant.ofEpochSecond(expirationTime));
            log.info("current time: {}", Instant.now());

            // URL 구성: {CDN_BASE_URL}/{storageKey}
            String fullUrl = String.format("%s/%s", cdn.baseUrl(), storageKey);

            // GCP Cloud CDN Signed URL 스펙:
            // 개별 파일의 경우 경로만 서명 (URLPrefix 없이)

            // 2. 쿼리 파라미터 구성 (서명용)
            String toSign = UriComponentsBuilder.fromUriString(fullUrl)
                    .queryParam("Expires", expirationTime)
                    .queryParam("KeyName", cdn.keyName())
                    .build(false)  // 인코딩하지 않음 (서명 전)
                    .toUriString();

            // 3. 서명 생성
            String signature = signUrl(toSign, cdn.privateKey());

            // 4. 최종 URL 구성
            String signedUrl = UriComponentsBuilder.fromUriString(fullUrl)
                    .queryParam("Expires", expirationTime)
                    .queryParam("KeyName", cdn.keyName())
                    .queryParam("Signature", signature)
                    .build(false)  // 인코딩하지 않음 (이미 URL-safe Base64)
                    .toUriString();

            log.info("CDN Signed URL 생성 완료");
            log.info("  Full URL: {}", fullUrl);
            log.info("  Expires: {} ({})", expirationTime, Instant.ofEpochSecond(expirationTime));
            log.info("  KeyName: {}", cdn.keyName());
            log.info("  To Sign: {}", toSign);
            log.info("  Signature: {}", signature);
            log.info("  Final URL: {}", signedUrl);

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
        // URL-safe Base64 디코딩 (하이픈, 언더스코어 포함)
        byte[] decodedKey = decodeBase64Key(privateKey.trim());

        // HMAC-SHA1 서명
        final String algorithm = "HmacSHA1";

        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, algorithm);
        mac.init(secretKeySpec);

        byte[] signatureBytes = mac.doFinal(urlToSign.getBytes(StandardCharsets.UTF_8));

        // URL-safe Base64 인코딩
        return Base64.getUrlEncoder().encodeToString(signatureBytes);
    }

    /**
     * Base64 키를 디코딩합니다.
     *
     * <p>URL-safe Base64(-, _) 우선 시도 후, 실패 시 표준 Base64(+, /) 시도
     */
    private byte[] decodeBase64Key(String key) {
        try {
            // URL-safe Base64 시도 (GCP 권장 형식)
            return Base64.getUrlDecoder().decode(key);
        } catch (IllegalArgumentException e) {
            // 표준 Base64로 fallback
            return Base64.getDecoder().decode(key);
        }
    }
}
