package com.umc.product.storage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileLockService")
class FileLockServiceTest {

    @Mock
    LoadFileMetadataPort loadFileMetadataPort;

    @Mock
    StoragePort storagePort;

    @InjectMocks
    FileLockService sut;

    @Test
    @DisplayName("정렬된 파일 ID로 잠근 뒤 사용 가능한 파일 정보를 반환한다")
    void lockUsableFilesInOrder() {
        FileMetadata first = uploadedFile("a-file", 1L);
        FileMetadata second = uploadedFile("b-file", 1L);
        given(loadFileMetadataPort.findByFileIdsForUpdate(List.of("a-file", "b-file")))
            .willReturn(List.of(first, second));
        given(storagePort.exists(first.getStorageKey())).willReturn(true);
        given(storagePort.exists(second.getStorageKey())).willReturn(true);

        var result = sut.lockUsableByIds(List.of("b-file", "a-file"), 1L);

        assertThat(result).extracting("fileId").containsExactly("b-file", "a-file");
    }

    @Test
    @DisplayName("잠근 파일 중 존재하지 않는 파일이 있으면 실패한다")
    void rejectMissingFile() {
        given(loadFileMetadataPort.findByFileIdsForUpdate(List.of("a-file", "missing")))
            .willReturn(List.of(uploadedFile("a-file", 1L)));

        assertThatThrownBy(() -> sut.lockUsableByIds(List.of("missing", "a-file"), 1L))
            .isInstanceOf(StorageException.class)
            .extracting("baseCode")
            .isEqualTo(StorageErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("메타데이터가 남아 있어도 스토리지 원본이 없으면 사용할 수 없다")
    void rejectFileWithoutStoredObject() {
        FileMetadata metadata = uploadedFile("missing-object", 1L);
        given(loadFileMetadataPort.findByFileIdsForUpdate(List.of("missing-object")))
            .willReturn(List.of(metadata));
        given(storagePort.exists(metadata.getStorageKey())).willReturn(false);

        assertThatThrownBy(() -> sut.lockUsableByIds(List.of("missing-object"), 1L))
            .isInstanceOf(StorageException.class)
            .extracting("baseCode")
            .isEqualTo(StorageErrorCode.FILE_NOT_FOUND);
    }

    private FileMetadata uploadedFile(String fileId, Long memberId) {
        FileMetadata metadata = FileMetadata.builder()
            .fileId(fileId)
            .originalFileName("evidence.png")
            .category(FileCategory.CHALLENGER_DEACTIVATION_EVIDENCE)
            .contentType("image/png")
            .fileSize(100L)
            .storageProvider(StorageProvider.AWS_S3)
            .storageKey("challenger-deactivation/" + fileId + ".png")
            .uploadedMemberId(memberId)
            .build();
        metadata.markAsUploaded();
        return metadata;
    }
}
