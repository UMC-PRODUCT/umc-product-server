# ADR-025: S3 Presigned PUT 업로드 크기 검증 강화

## Status

Accepted

## Context

2026년 6월 기준 storage 도메인은 파일 업로드 준비 API에서 클라이언트가 전달한 `fileSize`를 `FileCategory` 정책으로 검증한 뒤 S3 presigned PUT URL을 발급한다. 업로드 완료 API는 `FileCommandService.confirmUpload`에서 외부 스토리지 객체 존재 여부만 확인하고 `FileMetadata`를 업로드 완료 상태로 변경했다.

이 구조에서는 클라이언트가 `prepare-upload` 요청의 `fileSize`를 작게 보내고, 실제 presigned PUT 요청에는 더 큰 파일을 업로드하는 우회가 가능하다. `PORTFOLIO` 카테고리는 현재 200MB 제한을 유지하지만, 기존 구현은 실제 S3 객체의 `Content-Length`와 `Content-Type`을 confirm 단계에서 확인하지 않았기 때문에 메타데이터와 실제 객체가 불일치할 수 있었다.

이번 결정은 `com.umc.product.storage` 도메인 내부의 AWS S3 업로드 경로에 한정한다. GCS 어댑터는 추후 제거 예정이므로 이번 결정의 기능 범위에서 제외하되, 컴파일 호환성은 유지한다. 또한 Hexagonal Architecture 규칙에 따라 애플리케이션 서비스는 `StoragePort`를 통해서만 S3 객체 정보를 조회하고, S3 SDK 의존성은 `adapter.out.s3`에만 둔다.

### 문제점

1. **클라이언트 신고값만 신뢰하는 크기 검증**

   `prepare-upload` 단계의 `fileSize` 검증은 요청 값에만 의존한다. 사용자가 작은 크기를 신고하고 더 큰 파일을 직접 S3에 PUT하면, 서버는 confirm 단계에서 실제 객체 크기를 알 수 없었다.

2. **confirm 단계의 검증 부족**

   confirm은 객체 존재 여부만 확인했다. 이로 인해 실제 S3 객체의 크기와 Content-Type이 `FileMetadata`와 달라도 업로드 완료 상태로 저장될 수 있었다.

3. **presigned POST 전환의 API 호환성 비용**

   S3 presigned POST는 `content-length-range` 정책으로 상한을 더 명확히 강제할 수 있지만, 현재 클라이언트는 presigned PUT URL과 헤더 기반 업로드 흐름을 사용한다. 즉시 POST로 전환하면 응답 구조와 FE 업로드 구현 변경이 필요하다.

## Decision

우리는 현재 API 호환성을 유지하기 위해 S3 presigned PUT을 유지하되, 다음 방식으로 업로드 크기 검증을 강화하기로 결정한다.

1. `StoragePort`에 파일 크기를 포함하는 `generateUploadUrl(storageKey, contentType, fileSize, durationMinutes)` overload를 추가한다.
2. `S3StorageAdapter`는 `PutObjectRequest.contentLength(fileSize)`를 설정해 presigned PUT 요청에 정확한 업로드 길이를 포함한다.
3. `StoragePort`에 `findObjectInfoByStorageKey(storageKey)`를 추가하고, S3 구현은 `HeadObject` 결과의 `contentLength`와 `contentType`을 반환한다.
4. `FileCommandService.confirmUpload`는 실제 S3 객체 정보가 없으면 `FILE_UPLOAD_NOT_COMPLETED`, 크기 초과면 `FILE_SIZE_EXCEEDED`, 요청 크기와 실제 크기가 다르면 `FILE_SIZE_MISMATCH`, Content-Type의 MIME type이 다르면 `INVALID_CONTENT_TYPE`으로 실패시킨다.
5. 상태 변경과 실제 객체 정보 검증은 `FileMetadata.confirmUploaded(actualSize, actualContentType)` 도메인 메서드에 둔다.
6. 검증 실패 객체는 metadata를 업로드 완료로 저장하지 않고 best-effort로 S3에서 삭제한다.
7. S3 `HeadObject` 조회에서 404 계열은 객체 없음으로 처리하되, 그 외 S3 오류는 storage 도메인 예외(`STORAGE_METADATA_READ_FAILED`)로 변환한다.

### 단계적 진행 / PR 분할

- **Phase 1 (이 PR / 본 ADR)**: presigned PUT 유지, `Content-Length` 서명 반영, confirm 단계 `HeadObject` 검증 추가.
- **Phase 2 (별도 Issue #979)**: S3 presigned POST와 `content-length-range` 정책 도입 여부를 검토하고, 필요하면 별도 API 변경으로 진행한다.
- **Phase 3 (별도 Issue #983)**: 검증 실패 객체 정리를 DB 트랜잭션 외부에서 처리하기 위한 outbox 패턴 도입을 검토한다.

## Alternatives Considered

### 대안 A: 현행 유지

prepare 단계의 신고 `fileSize`만 검증하고 confirm에서는 객체 존재 여부만 확인한다.

장점:

- API와 클라이언트 구현 변경이 없다.
- 서버 코드 변경량이 가장 작다.

단점:

- 실제 S3 객체 크기 우회를 막지 못한다.
- DB 메타데이터와 S3 객체 정보가 달라질 수 있다.
- 대용량 파일 업로드로 스토리지 비용과 운영 리스크가 증가할 수 있다.

선택하지 않은 이유:

- #252에서 제기된 핵심 문제가 실제 객체 정보 미검증이므로, 현행 유지는 문제를 해결하지 못한다.

### 대안 B: 즉시 S3 presigned POST로 전환

S3 POST policy의 `content-length-range` 조건으로 업로드 단계에서 파일 크기 상한을 강제한다.

장점:

- 업로드 URL 단계에서 카테고리별 최대 크기를 정책 조건으로 표현할 수 있다.
- 파일 크기 상한 정책이 S3에 더 명시적으로 위임된다.

단점:

- 기존 presigned PUT 응답과 클라이언트 업로드 방식이 바뀐다.
- FE는 URL 단일 PUT이 아니라 form fields 기반 POST 업로드를 구현해야 한다.
- 기존 API 하위 호환성, 문서, 테스트 범위가 커진다.

선택하지 않은 이유:

- 현재 버그 대응은 우회 업로드 차단이 우선이며, POST 전환은 API 계약 변경이 크다. 따라서 #979로 분리해 별도 설계와 리뷰를 진행한다.

### 대안 C: confirm 검증만 추가

presigned PUT 생성은 그대로 두고, confirm 단계에서만 `HeadObject` 결과를 검증한다.

장점:

- 클라이언트 영향이 없다.
- 실제 저장된 객체 기준으로 최종 무결성을 보장할 수 있다.

단점:

- S3 업로드 자체는 먼저 발생하므로, 초과 파일이 일시적으로 저장될 수 있다.
- 실패 객체 삭제가 실패하면 운영상 orphan 객체가 남을 수 있다.

선택하지 않은 이유:

- 최종 방어선으로는 필요하지만, presigned PUT 생성 시 요청 길이도 함께 반영하는 편이 현재 API를 유지하면서 할 수 있는 선제 방어를 추가한다.

## Consequences

### Positive

- confirm 단계에서 실제 S3 객체 크기와 Content-Type을 기준으로 업로드 완료 여부를 결정한다.
- `FileMetadata`에 저장된 파일 크기와 실제 S3 객체 크기의 불일치를 막는다.
- 기존 presigned PUT API와 클라이언트 업로드 방식은 유지한다.
- GCS 제거 전까지도 인터페이스 default method로 컴파일 호환성을 유지한다.

### Negative

- confirm 단계에서 S3 `HeadObject` 호출이 추가되어 업로드 완료 API의 외부 I/O가 증가한다.
- 초과 파일은 S3에 먼저 업로드된 뒤 confirm에서 삭제되므로, 업로드 시점의 네트워크/스토리지 비용을 완전히 제거하지는 못한다.
- `Content-Length` 서명은 정확한 요청 길이를 기대하므로, 일부 비브라우저 클라이언트가 chunked transfer로 PUT하면 업로드 실패 가능성이 있다.

### Neutral / Trade-offs

- `FILE_SIZE_MISMATCH` 신규 에러 코드를 추가해 크기 초과와 크기 불일치를 분리했다. 클라이언트는 더 정확한 실패 원인을 받을 수 있지만, 에러 카탈로그와 문서 갱신이 필요하다.
- Content-Type 비교는 전체 문자열이 아니라 MIME type을 기준으로 한다. 예를 들어 `application/pdf`와 `application/pdf; charset=UTF-8`은 같은 타입으로 본다.
- `STORAGE_METADATA_READ_FAILED` 신규 에러 코드를 추가해 S3 객체 정보 조회 실패를 도메인 예외로 일관되게 노출한다.
- presigned POST는 더 강한 업로드 단계 정책을 제공하지만, 이번 PR에서는 호환성 유지를 우선한다.

## Implementation Notes

### 변경 영역 요약

1. **도메인** (`com.umc.product.storage.domain.*`): `FileMetadata.confirmUploaded`에서 실제 크기와 Content-Type 검증 후 업로드 완료 처리.
2. **응용 / Port** (`...application.service.*`, `...application.port.*`): `StoragePort`에 파일 크기 포함 URL 생성 overload와 S3 객체 정보 조회 메서드 추가.
3. **어댑터 (out)** (`...adapter.out.s3.*`): `S3StorageAdapter`에서 `PutObjectRequest.contentLength` 설정, `HeadObject` 기반 `StorageObjectInfo` 반환.
4. **테스트** (`src/test/...`): 서비스/도메인/S3 어댑터 단위 테스트와 기존 storage 통합 테스트 스텁 갱신.
5. **문서** (`docs/guides`, `src/main/resources/static/docs/catalog/error`): 신규 `FILE_SIZE_MISMATCH`, `STORAGE_METADATA_READ_FAILED` 에러 코드 카탈로그 갱신.

### 기타 참고

- `FileUploadInfo.headers`에는 브라우저에서 직접 설정 가능한 `Content-Type`만 유지한다. `Content-Length`는 브라우저 forbidden header이므로 응답 헤더 목록에 강제로 추가하지 않는다.
- 검증 실패 시 S3 객체 삭제는 best-effort로 수행하며, 삭제 실패가 원래 검증 예외를 덮어쓰지 않게 한다.
- best-effort 삭제는 현재 트랜잭션 안에서 호출된다. 트랜잭션 경계와 외부 I/O 분리는 #983에서 outbox 패턴으로 별도 검토한다.

## References

- 원 이슈: [#252](https://github.com/UMC-PRODUCT/umc-product-server/issues/252)
- 후속 Issue: [#979](https://github.com/UMC-PRODUCT/umc-product-server/issues/979)
- 후속 Issue: [#983](https://github.com/UMC-PRODUCT/umc-product-server/issues/983)
- AWS S3 POST policy: [Browser-Based Uploads Using POST](https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-HTTPPOSTConstructPolicy.html)
