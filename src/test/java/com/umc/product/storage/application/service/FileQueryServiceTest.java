package com.umc.product.storage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import com.umc.product.storage.domain.exception.StorageException;
import com.umc.product.support.UseCaseTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileQueryService 통합 테스트
 */
@Transactional
class FileQueryServiceTest extends UseCaseTestSupport {
    // Autowired = 실제 서비스 주입
    // MockitoBean = Mock 객체 주입

    @Autowired
    private GetFileUseCase getFileUseCase;

    @Autowired
    private SaveFileMetadataPort saveFileMetadataPort;

    @Autowired
    private LoadFileMetadataPort loadFileMetadataPort;

    @MockitoBean
    private StoragePort storagePort;


    @Test
    void 파일_ID로_파일_정보를_조회한다() {
        // given: 파일 메타데이터를 DB에 저장
        FileMetadata savedMetadata = saveTestFile("test-file-1", "profile.jpg");

        // StoragePort의 generateAccessUrl이 호출되면 Mock URL 반환
        given(storagePort.generateAccessUrl(anyString(), anyLong()))
            .willReturn("https://cdn.example.com/signed-url");

        // when: 파일 조회
        FileInfo result = getFileUseCase.getById(savedMetadata.getId());

        // then: 반환된 정보 검증
        assertThat(result).isNotNull();
        assertThat(result.fileId()).isEqualTo(savedMetadata.getId());
        assertThat(result.originalFileName()).isEqualTo("profile.jpg");
        assertThat(result.category()).isEqualTo(FileCategory.PROFILE_IMAGE);
        assertThat(result.fileLink()).isEqualTo("https://cdn.example.com/signed-url");
    }

    /**
     * ✅ 학습 포인트 5: 예외 케이스 테스트 - assertThatThrownBy로 예외 발생을 검증합니다
     */
    @Test
    void 존재하지_않는_파일_ID로_조회하면_예외가_발생한다() {
        // given: 존재하지 않는 파일 ID
        String nonExistentFileId = "non-existent-id";

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> getFileUseCase.getById(nonExistentFileId))
            .isInstanceOf(StorageException.class);
    }

    @Test
    void 파일_존재_여부를_확인한다() {
        // given
        FileMetadata savedMetadata = saveTestFile("test-file-2", "document.pdf");

        // when
        boolean exists = getFileUseCase.existsById(savedMetadata.getId());
        boolean notExists = getFileUseCase.existsById("non-existent-id");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    /**
     * ✅ 학습 포인트 6: 테스트 헬퍼 메서드 - 반복되는 테스트 데이터 생성 로직을 메서드로 추출 - 테스트 가독성 향상
     */
    private FileMetadata saveTestFile(String fileId, String fileName) {
        FileMetadata metadata = FileMetadata.builder()
            .fileId(fileId)
            .originalFileName(fileName)
            .category(FileCategory.PROFILE_IMAGE)
            .contentType("image/jpeg")
            .fileSize(1024L)
            .storageProvider(StorageProvider.GOOGLE_CLOUD_STORAGE)
            .storageKey("profile/" + fileId + ".jpg")
            .uploadedMemberId(1L)
            .build();

        return saveFileMetadataPort.save(metadata);
    }
}
