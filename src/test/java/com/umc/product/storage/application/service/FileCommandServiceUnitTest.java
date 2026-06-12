package com.umc.product.storage.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.storage.application.port.in.command.dto.DeleteFileCommand;
import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileCommandServiceUnitTest {

    @Mock
    StoragePort storagePort;

    @Mock
    LoadFileMetadataPort loadFileMetadataPort;

    @Mock
    SaveFileMetadataPort saveFileMetadataPort;

    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;

    @InjectMocks
    FileCommandService sut;

    @Test
    @DisplayName("작성자가 아니어도 SUPER_ADMIN이면 파일을 삭제한다")
    void 작성자가_아니어도_SUPER_ADMIN이면_파일을_삭제한다() {
        // given
        FileMetadata metadata = uploadedFile("file-id", 1L);
        given(loadFileMetadataPort.findByFileId("file-id")).willReturn(Optional.of(metadata));
        given(getChallengerRoleUseCase.findAllByMemberId(2L)).willReturn(List.of(role(ChallengerRoleType.SUPER_ADMIN)));

        // when
        sut.deleteFile(deleteCommand("file-id", 2L));

        // then
        then(storagePort).should().delete(metadata.getStorageKey());
        then(saveFileMetadataPort).should().deleteByFileId("file-id");
    }

    @Test
    @DisplayName("작성자도 SUPER_ADMIN도 아니면 파일을 삭제할 수 없다")
    void 작성자도_SUPER_ADMIN도_아니면_파일을_삭제할_수_없다() {
        // given
        FileMetadata metadata = uploadedFile("file-id", 1L);
        given(loadFileMetadataPort.findByFileId("file-id")).willReturn(Optional.of(metadata));
        given(getChallengerRoleUseCase.findAllByMemberId(2L)).willReturn(List.of(role(ChallengerRoleType.SCHOOL_PRESIDENT)));

        // when & then
        assertThatThrownBy(() -> sut.deleteFile(deleteCommand("file-id", 2L)))
            .isInstanceOf(StorageException.class)
            .extracting("baseCode")
            .isEqualTo(StorageErrorCode.FILE_DELETE_FORBIDDEN);

        then(storagePort).should(never()).delete(anyString());
        then(saveFileMetadataPort).should(never()).deleteByFileId(anyString());
    }

    @Test
    @DisplayName("S3 삭제가 실패하면 파일 메타데이터를 삭제하지 않는다")
    void S3_삭제가_실패하면_파일_메타데이터를_삭제하지_않는다() {
        // given
        FileMetadata metadata = uploadedFile("file-id", 1L);
        given(loadFileMetadataPort.findByFileId("file-id")).willReturn(Optional.of(metadata));
        willThrow(new StorageException(StorageErrorCode.STORAGE_DELETE_FAILED))
            .given(storagePort)
            .delete(metadata.getStorageKey());

        // when & then
        assertThatThrownBy(() -> sut.deleteFile(deleteCommand("file-id", 1L)))
            .isInstanceOf(StorageException.class)
            .extracting("baseCode")
            .isEqualTo(StorageErrorCode.STORAGE_DELETE_FAILED);

        then(saveFileMetadataPort).should(never()).deleteByFileId(anyString());
    }

    private DeleteFileCommand deleteCommand(String fileId, Long requesterMemberId) {
        return DeleteFileCommand.builder()
            .fileId(fileId)
            .requesterMemberId(requesterMemberId)
            .build();
    }

    private ChallengerRoleInfo role(ChallengerRoleType roleType) {
        return ChallengerRoleInfo.builder()
            .id(1L)
            .challengerId(1L)
            .roleType(roleType)
            .organizationType(OrganizationType.CENTRAL)
            .gisuId(1L)
            .build();
    }

    private FileMetadata uploadedFile(String fileId, Long uploadedMemberId) {
        FileMetadata metadata = FileMetadata.builder()
            .fileId(fileId)
            .originalFileName("document.pdf")
            .category(FileCategory.ETC)
            .contentType("application/pdf")
            .fileSize(1024L)
            .storageProvider(StorageProvider.AWS_S3)
            .storageKey("test/" + fileId + ".pdf")
            .uploadedMemberId(uploadedMemberId)
            .build();
        metadata.markAsUploaded();
        return metadata;
    }
}
