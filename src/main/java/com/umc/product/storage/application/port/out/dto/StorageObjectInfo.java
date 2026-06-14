package com.umc.product.storage.application.port.out.dto;

import java.util.Objects;

/**
 * 외부 스토리지에 실제로 저장된 객체의 메타데이터입니다.
 */
public record StorageObjectInfo(
    String storageKey,
    long contentLength,
    String contentType
) {

    public StorageObjectInfo {
        Objects.requireNonNull(storageKey, "storageKey must not be null");
    }

    public static StorageObjectInfo of(String storageKey, long contentLength, String contentType) {
        return new StorageObjectInfo(storageKey, contentLength, contentType);
    }
}
