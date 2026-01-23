package com.umc.product.storage.domain.exception;

import com.umc.product.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StorageErrorCode implements BaseCode {

    // 파일 관련 에러
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORAGE-0001", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "STORAGE-0002", "파일 업로드가 완료되지 않았습니다."),
    FILE_ALREADY_UPLOADED(HttpStatus.BAD_REQUEST, "STORAGE-0003", "이미 업로드가 완료된 파일입니다."),

    // 파일 검증 에러
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "STORAGE-0004", "허용되지 않는 파일 확장자입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "STORAGE-0005", "파일 크기가 허용 범위를 초과했습니다."),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "STORAGE-0006", "잘못된 Content-Type입니다."),

    // 스토리지 에러
    STORAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE-0007", "파일 업로드에 실패했습니다."),
    STORAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE-0008", "파일 삭제에 실패했습니다."),
    STORAGE_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE-0009", "파일 접근 URL 생성에 실패했습니다."),

    // CDN 에러
    CDN_SIGNING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE-0010", "CDN Signed URL 생성에 실패했습니다."),
    NO_ENV_KEYS(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE-0011", "CDN이 활성화되어 있지만 관련 환경변수가 설정되어 있지 않습니다. 관리자에게 문의하세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
