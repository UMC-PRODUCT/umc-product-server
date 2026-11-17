# Storage 테스트 케이스

- 테스트 파일: 9개
- 테스트 케이스: 58개
- 분류 기준: `Controller`, `UseCase`, `Repository`, `E2E`, `Scheduler`, `Domain`, `External Adapter`, `Support`

| 카테고리 | 케이스 수 |
|---|---:|
| UseCase / Application Service | 34 |
| Repository / Outbound Persistence | 1 |
| Domain | 14 |
| External Adapter | 9 |

## UseCase / Application Service

### FileCommandServiceTest
- 위치: `monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [42](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L42) | 파일 업로드 URL을 생성한다 | PrepareFileUploadCommand("profile.jpg", "image/jpeg", 1024L, FileCategory.PROFILE_IMAGE, 1L); 호출 getFileUploadUrl(command) | 성공: 검증 assertThat(result).isNotNull(); assertThat(result.uploadUrl()).contains("signed-upload-url"); assertThat(result.uploadMethod()).isEqualTo("PUT"); assertThat(savedMetadata.getOriginalFileName()).isEqualTo("profile.jpg"... |
| [86](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L86) | 허용되지 않는 확장자면 예외가 발생한다 | PrepareFileUploadCommand("profile.gif", // GIF는 허용 안됨 "image/gif", 1024L, FileCategory.PROFILE_IMAGE, 1L); 호출 getFileUploadUrl(command)) | 실패: 예외 StorageException |
| [101](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L101) | 파일 크기가 초과하면 예외가 발생한다 | PrepareFileUploadCommand("large-profile.jpg", "image/jpeg", fileSize, FileCategory.PROFILE_IMAGE, 1L); 호출 getFileUploadUrl(command)) | 실패: 예외 StorageException |
| [118](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L118) | 확장자가 없으면 예외가 발생한다 | PrepareFileUploadCommand("profile", // 확장자 없음 "image/jpeg", 1024L, FileCategory.PROFILE_IMAGE, 1L); 호출 getFileUploadUrl(command)) | 실패: 예외 StorageException |
| [138](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L138) | 업로드를 완료 처리한다 | 호출 confirmUpload(metadata.getId()) | 성공: 검증 assertThat(updated.isUploaded()).isTrue(); |
| [159](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L159) | 스토리지에 파일이 없으면 업로드 완료 처리에 실패한다 | 호출 confirmUpload(metadata.getId())) | 실패: 예외 StorageException |
| [173](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L173) | 이미 업로드된 파일은 재확인할 수 없다 | 호출 confirmUpload(metadata.getId())) | 실패: 예외 StorageException |
| [187](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L187) | 파일을 삭제한다 | 호출 deleteFile(deleteCommand(fileId, 1L)) | 성공: 검증 assertThat(loadFileMetadataPort.findByFileId(fileId)).isEmpty(); |
| [202](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceTest.java#L202) | 존재하지 않는 파일을 삭제하면 예외가 발생한다 | 호출 deleteFile(deleteCommand(nonExistentFileId, 1L))) | 실패: 예외 StorageException |

### FileCommandServiceUnitTest
- 위치: `monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [73](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L73) | 파일 명령은 메타데이터 잠금이 필요한 작업에 트랜잭션을 적용한다 | 업로드 확인·파일 삭제 메서드의 트랜잭션 선언 확인 | 성공: 잠금이 필요한 명령 메서드에 `@Transactional` 적용 |
| [88](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L88) | 업로드 URL 생성은 요청 파일 크기를 스토리지 서명에 포함한다 | PrepareFileUploadCommand("portfolio.pdf", "application/pdf", 1024L, FileCategory.PORTFOLIO, 1L); 호출 getFileUploadUrl(command) | 성공: 업로드 URL 생성은 요청 파일 크기를 스토리지 서명에 포함한다 |
| [124](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L124) | 업로드 완료 확인 시 S3 객체가 없으면 완료 처리하지 않는다 | 호출 confirmUpload("file-id")) | 실패: 예외 StorageException; 에러코드 StorageErrorCode.FILE_UPLOAD_NOT_COMPLETED; 검증 .isEqualTo(StorageErrorCode.FILE_UPLOAD_NOT_COMPLETED); |
| [142](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L142) | 실제 S3 객체 크기가 카테고리 제한을 초과하면 업로드 완료 처리하지 않고 객체를 삭제한다 | 호출 confirmUpload("file-id")) | 실패: 예외 StorageException; 에러코드 StorageErrorCode.FILE_SIZE_EXCEEDED; 검증 .isEqualTo(StorageErrorCode.FILE_SIZE_EXCEEDED); |
| [162](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L162) | 실제 S3 객체 크기가 요청 크기와 다르면 업로드 완료 처리하지 않고 객체를 삭제한다 | 호출 confirmUpload("file-id")) | 실패: 예외 StorageException; 에러코드 StorageErrorCode.FILE_SIZE_MISMATCH; 검증 .isEqualTo(StorageErrorCode.FILE_SIZE_MISMATCH); |
| [181](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L181) | 실제 S3 객체 Content-Type이 요청값과 다르면 업로드 완료 처리하지 않고 객체를 삭제한다 | 호출 confirmUpload("file-id")) | 실패: 예외 StorageException; 에러코드 StorageErrorCode.INVALID_CONTENT_TYPE; 검증 .isEqualTo(StorageErrorCode.INVALID_CONTENT_TYPE); |
| [200](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L200) | 실제 S3 객체 정보가 요청값과 일치하면 업로드 완료 처리한다 | 호출 confirmUpload("file-id") | 성공: 검증 assertThat(captor.getValue().isUploaded()).isTrue(); |
| [219](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L219) | 작성자가 아니어도 SUPER_ADMIN이면 파일을 삭제한다 | 호출 deleteFile(deleteCommand("file-id", 2L)) | 성공: 작성자가 아니어도 SUPER_ADMIN이면 파일을 삭제한다 |
| [235](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L235) | 작성자도 SUPER_ADMIN도 아니면 파일을 삭제할 수 없다 | 호출 deleteFile(deleteCommand("file-id", 2L))) | 실패: 예외 StorageException; 에러코드 StorageErrorCode.FILE_DELETE_FORBIDDEN; 검증 .isEqualTo(StorageErrorCode.FILE_DELETE_FORBIDDEN); |
| [253](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L253) | S3 삭제가 실패하면 파일 메타데이터를 삭제하지 않는다 | 호출 deleteFile(deleteCommand("file-id", 1L))) | 실패: 예외 StorageException; 에러코드 StorageErrorCode.STORAGE_DELETE_FAILED; 검증 .isEqualTo(StorageErrorCode.STORAGE_DELETE_FAILED); |
| [238](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L238) | 탈부 근거로 사용 중인 파일은 작성자도 삭제할 수 없다 | 공통 삭제 API로 탈부 근거 파일 삭제 | 실패: 탈부 근거 파일 삭제 금지 |
| [256](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L256) | 탈부 근거로 사용 중인 파일은 최고 관리자도 공통 API로 삭제할 수 없다 | 최고 관리자가 공통 삭제 API 호출 | 실패: 탈부 근거 파일 삭제 금지 |
| [312](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L312) | 시스템 삭제는 파일이 존재하면 스토리지와 메타데이터를 삭제한다 | 다른 탈부 기록의 참조가 없는 파일 | 성공: 원본과 메타데이터 삭제 |
| [324](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L324) | 다른 탈부 기록이 참조하는 파일은 시스템 삭제하지 않는다 | 다른 탈부 기록의 동일 fileId 참조 존재 | 성공: 원본과 메타데이터 유지 |
| [339](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileCommandServiceUnitTest.java#L339) | 시스템 삭제는 파일이 이미 없으면 성공으로 처리한다 | 존재하지 않는 fileId | 성공: 멱등 처리 |

### FileLockServiceTest
- 위치: `monolith/src/test/java/com/umc/product/storage/application/service/FileLockServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [34](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileLockServiceTest.java#L34) | 정렬된 파일 ID로 잠근 뒤 사용 가능한 파일 정보를 반환한다 | 역순으로 전달된 파일 ID 목록 | 성공: 정렬된 순서로 잠금 획득 후 요청 순서로 반환 |
| [47](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileLockServiceTest.java#L47) | 잠근 파일 중 존재하지 않는 파일이 있으면 실패한다 | 일부 파일 ID가 존재하지 않음 | 실패: `FILE_NOT_FOUND` |

### FileQueryServiceTest
- 위치: `monolith/src/test/java/com/umc/product/storage/application/service/FileQueryServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [35](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileQueryServiceTest.java#L35) | 파일 ID로 파일 정보를 조회한다 | 조건 파일 ID로 파일 정보를 조회한다 | 성공: 검증 assertThat(result).isNotNull(); assertThat(result.fileId()).isEqualTo(savedMetadata.getId()); assertThat(result.originalFileName()).isEqualTo("profile.jpg"); assertThat(result.category()).isEqualTo(FileCategory.PROFIL... |
| [60](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileQueryServiceTest.java#L60) | 존재하지 않는 파일 ID로 조회하면 예외가 발생한다 | 조건 존재하지 않는 파일 ID로 조회하면 예외가 발생한다 | 실패: 예외 StorageException |
| [69](../../../monolith/src/test/java/com/umc/product/storage/application/service/FileQueryServiceTest.java#L69) | 파일 존재 여부를 확인한다 | 조건 파일 존재 여부를 확인한다 | 성공: 검증 assertThat(exists).isTrue(); assertThat(notExists).isFalse(); |

## Repository / Outbound Persistence

### FileMetadataLockIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/storage/adapter/out/persistence/FileMetadataLockIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [32](../../../monolith/src/test/java/com/umc/product/storage/adapter/out/persistence/FileMetadataLockIntegrationTest.java#L32) | 같은 파일의 쓰기 잠금은 앞선 트랜잭션이 끝날 때까지 대기한다 | 두 트랜잭션이 같은 fileId에 쓰기 잠금 요청 | 성공: 두 번째 트랜잭션은 첫 번째 트랜잭션 종료 후 잠금 획득 |

## Domain

### FileMetadataTest
- 위치: `monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [22](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L22) | 업로드 완료 처리를 한다 | 조건 업로드 완료 처리를 한다 | 성공: 검증 assertThat(metadata.isUploaded()).isTrue(); |
| [42](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L42) | 실제 파일 정보가 일치하면 업로드 완료 처리를 한다 | 조건 실제 파일 정보가 일치하면 업로드 완료 처리를 한다 | 성공: 검증 assertThat(metadata.isUploaded()).isTrue(); |
| [54](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L54) | 실제 파일 크기가 카테고리 제한을 초과하면 예외가 발생한다 | 조건 실제 파일 크기가 카테고리 제한을 초과하면 예외가 발생한다 | 실패: 예외 StorageException; 에러코드 StorageErrorCode.FILE_SIZE_EXCEEDED; 검증 .isEqualTo(StorageErrorCode.FILE_SIZE_EXCEEDED); |
| [66](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L66) | 실제 파일 크기가 요청 크기와 다르면 예외가 발생한다 | 조건 실제 파일 크기가 요청 크기와 다르면 예외가 발생한다 | 실패: 예외 StorageException; 에러코드 StorageErrorCode.FILE_SIZE_MISMATCH; 검증 .isEqualTo(StorageErrorCode.FILE_SIZE_MISMATCH); |
| [78](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L78) | 요청 파일 크기가 null이면 예외가 발생한다 | 조건 요청 파일 크기가 null이면 예외가 발생한다 | 실패: 예외 StorageException; 에러코드 StorageErrorCode.FILE_SIZE_MISMATCH; 검증 .isEqualTo(StorageErrorCode.FILE_SIZE_MISMATCH); |
| [90](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L90) | 실제 Content Type이 요청값과 다르면 예외가 발생한다 | 조건 실제 Content Type이 요청값과 다르면 예외가 발생한다 | 실패: 예외 StorageException; 에러코드 StorageErrorCode.INVALID_CONTENT_TYPE; 검증 .isEqualTo(StorageErrorCode.INVALID_CONTENT_TYPE); |
| [102](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L102) | 실제 Content Type에 파라미터가 있어도 미디어 타입이 같으면 업로드 완료 처리를 한다 | 조건 실제 Content Type에 파라미터가 있어도 미디어 타입이 같으면 업로드 완료 처리를 한다 | 성공: 검증 assertThat(metadata.isUploaded()).isTrue(); |
| [114](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L114) | 요청 Content Type에 파라미터가 있어도 미디어 타입이 같으면 업로드 완료 처리를 한다 | 조건 요청 Content Type에 파라미터가 있어도 미디어 타입이 같으면 업로드 완료 처리를 한다 | 성공: 검증 assertThat(metadata.isUploaded()).isTrue(); |
| [135](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L135) | 파일 확장자를 추출한다 | 조건 파일 확장자를 추출한다 | 성공: 검증 assertThat(extension).isEqualTo("pdf"); |
| [146](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L146) | 확장자가 대문자면 소문자로 변환한다 | 조건 확장자가 대문자면 소문자로 변환한다 | 성공: 검증 assertThat(extension).isEqualTo("png"); |
| [162](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L162) | 확장자가 없으면 빈 문자열을 반환한다 | 조건 확장자가 없으면 빈 문자열을 반환한다 | 성공: 검증 assertThat(extension).isEmpty(); |
| [173](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L173) | 파일명이 점으로 시작하면 빈 문자열을 반환한다 | 조건 파일명이 점으로 시작하면 빈 문자열을 반환한다 | 성공: 검증 assertThat(extension).isEmpty(); |
| [185](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L185) | 파일명이 점으로 끝나면 빈 문자열을 반환한다 | 조건 파일명이 점으로 끝나면 빈 문자열을 반환한다 | 성공: 검증 assertThat(extension).isEmpty(); |
| [197](../../../monolith/src/test/java/com/umc/product/storage/domain/FileMetadataTest.java#L197) | 여러 개의 점이 있으면 마지막 확장자를 추출한다 | 조건 여러 개의 점이 있으면 마지막 확장자를 추출한다 | 성공: 검증 assertThat(extension).isEqualTo("gz"); |

## External Adapter

### S3StorageAdapterTest
- 위치: `monolith/src/test/java/com/umc/product/storage/adapter/out/s3/S3StorageAdapterTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [42](../../../monolith/src/test/java/com/umc/product/storage/adapter/out/s3/S3StorageAdapterTest.java#L42) | Presigned PUT 생성 시 요청 파일 크기를 Content-Length로 서명한다 | 호출 generateUploadUrl("private/portfolio/file.pdf", "application/pdf", 1024L, 15L) | 성공: 검증 assertThat(result.uploadUrl()).isEqualTo("https://storage.example.com/upload"); assertThat(result.uploadMethod()).isEqualTo("PUT"); assertThat(result.headers()).containsEntry("Content-Type", "application/pdf"); assert... |
| [64](../../../monolith/src/test/java/com/umc/product/storage/adapter/out/s3/S3StorageAdapterTest.java#L64) | HeadObject 결과를 S3 객체 정보로 반환한다 | 호출 findObjectInfoByStorageKey("private/portfolio/file.pdf") | 성공: 검증 assertThat(result).hasValue(StorageObjectInfo.of("private/portfolio/file.pdf", 1024L, "application/pdf")); |
| [82](../../../monolith/src/test/java/com/umc/product/storage/adapter/out/s3/S3StorageAdapterTest.java#L82) | S3 객체가 없으면 빈 객체 정보를 반환하고 exists는 false를 반환한다 | 호출 findObjectInfoByStorageKey("private/portfolio/missing.pdf")).isEmpty(); 호출 exists("private/portfolio/missing.pdf")).isFalse() | 성공: 검증 assertThat(sut.findObjectInfoByStorageKey("private/portfolio/missing.pdf")).isEmpty(); assertThat(sut.exists("private/portfolio/missing.pdf")).isFalse(); |
| [95](../../../monolith/src/test/java/com/umc/product/storage/adapter/out/s3/S3StorageAdapterTest.java#L95) | HeadObject 조회 중 404가 아닌 S3Exception은 StorageException으로 변환한다 | 호출 findObjectInfoByStorageKey("private/portfolio/file.pdf")) | 실패: 예외 StorageException; 에러코드 StorageErrorCode.STORAGE_METADATA_READ_FAILED; 검증 .isEqualTo(StorageErrorCode.STORAGE_METADATA_READ_FAILED); |
