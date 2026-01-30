package com.umc.product.storage.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import org.junit.jupiter.api.Test;

/**
 * FileMetadata Entity의 도메인 로직을 테스트합니다.
 * <p>
 * 테스트 방법: 1. given: 테스트 대상 객체를 생성합니다 2. when: 테스트하려는 메서드를 호출합니다 3. then: 결과를 검증합니다 (assertThat 사용)
 */
class FileMetadataTest {

    /**
     * ✅ 학습 포인트 1: 가장 기본적인 단위 테스트 - 도메인 메서드의 동작을 검증합니다 - 외부 의존성이 없어서 가장 쉽습니다
     */
    @Test
    void 업로드_완료_처리를_한다() {
        // given: 업로드되지 않은 파일 메타데이터 생성
        FileMetadata metadata = FileMetadata.builder()
            .fileId("test-file-id")
            .originalFileName("test.jpg")
            .category(FileCategory.PROFILE_IMAGE)
            .contentType("image/jpeg")
            .fileSize(1024L)
            .storageProvider(StorageProvider.GOOGLE_CLOUD_STORAGE)
            .storageKey("profile/test-file-id.jpg")
            .uploadedMemberId(1L)
            .build();

        // when: 업로드 완료 처리
        metadata.markAsUploaded();

        // then: isUploaded가 true가 되었는지 확인
        assertThat(metadata.isUploaded()).isTrue();
    }

    /**
     * ✅ 학습 포인트 2: 경계값 테스트 (정상 케이스) - 다양한 입력값으로 테스트합니다
     */
    @Test
    void 파일_확장자를_추출한다() {
        // given
        FileMetadata metadata = createFileMetadata("document.pdf");

        // when
        String extension = metadata.getFileExtension();

        // then
        assertThat(extension).isEqualTo("pdf");
    }

    @Test
    void 확장자가_대문자면_소문자로_변환한다() {
        // given
        FileMetadata metadata = createFileMetadata("Image.PNG");

        // when
        String extension = metadata.getFileExtension();

        // then
        assertThat(extension).isEqualTo("png");
    }

    /**
     * ✅ 학습 포인트 3: 예외 케이스 테스트 - 엣지 케이스를 테스트합니다
     */
    @Test
    void 확장자가_없으면_빈_문자열을_반환한다() {
        // given
        FileMetadata metadata = createFileMetadata("README");

        // when
        String extension = metadata.getFileExtension();

        // then
        assertThat(extension).isEmpty();
    }

    @Test
    void 파일명이_점으로_시작하면_빈_문자열을_반환한다() {
        // given: 숨김 파일
        FileMetadata metadata = createFileMetadata(".gitignore");

        // when
        String extension = metadata.getFileExtension();

        // then
        assertThat(extension).isEmpty();
    }

    @Test
    void 파일명이_점으로_끝나면_빈_문자열을_반환한다() {
        // given
        FileMetadata metadata = createFileMetadata("file.");

        // when
        String extension = metadata.getFileExtension();

        // then
        assertThat(extension).isEmpty();
    }

    @Test
    void 여러_개의_점이_있으면_마지막_확장자를_추출한다() {
        // given
        FileMetadata metadata = createFileMetadata("archive.tar.gz");

        // when
        String extension = metadata.getFileExtension();

        // then
        assertThat(extension).isEqualTo("gz");
    }

    /**
     * ✅ 학습 포인트 4: 테스트 헬퍼 메서드 - 반복되는 객체 생성 로직을 메서드로 추출합니다 - 테스트 코드의 가독성이 높아집니다
     */
    private FileMetadata createFileMetadata(String fileName) {
        return FileMetadata.builder()
            .fileId("test-id")
            .originalFileName(fileName)
            .category(FileCategory.PROFILE_IMAGE)
            .contentType("application/octet-stream")
            .fileSize(1024L)
            .storageProvider(StorageProvider.GOOGLE_CLOUD_STORAGE)
            .storageKey("test/key")
            .uploadedMemberId(1L)
            .build();
    }
}
