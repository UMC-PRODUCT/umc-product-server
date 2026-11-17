package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationHistoryJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationImageJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerDeactivationLinkJpaRepository;
import com.umc.product.challenger.application.port.in.command.AddChallengerDeactivationEvidenceUseCase;
import com.umc.product.challenger.application.port.in.command.DeleteChallengerDeactivationDataUseCase;
import com.umc.product.challenger.application.port.in.command.dto.AddChallengerDeactivationEvidenceCommand;
import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SaveChallengerDeactivationPort;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.ChallengerDeactivationImage;
import com.umc.product.challenger.domain.ChallengerDeactivationLink;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationHistoryAction;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.storage.adapter.out.persistence.FileMetadataRepository;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("탈부 보관 만료 삭제 트랜잭션")
class ChallengerDeactivationDeletionIntegrationTest extends IntegrationTestSupport {

    @Autowired
    DeleteChallengerDeactivationDataUseCase deleteChallengerDeactivationDataUseCase;

    @Autowired
    AddChallengerDeactivationEvidenceUseCase addChallengerDeactivationEvidenceUseCase;

    @Autowired
    SaveChallengerDeactivationPort saveChallengerDeactivationPort;

    @Autowired
    ChallengerDeactivationJpaRepository deactivationRepository;

    @Autowired
    ChallengerDeactivationHistoryJpaRepository historyRepository;

    @Autowired
    ChallengerDeactivationImageJpaRepository imageRepository;

    @Autowired
    ChallengerDeactivationLinkJpaRepository linkRepository;

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @MockitoSpyBean
    DeleteChallengerDeactivationPort deleteChallengerDeactivationPort;

    @Test
    @DisplayName("자식 기록 삭제 중 실패하면 탈부 기록과 모든 근거 자료를 복구한다")
    void rollbackAllDataWhenChildDeletionFails() {
        ChallengerDeactivation deactivation = saveChallengerDeactivationPort.save(
            ChallengerDeactivation.create(
                1L,
                ChallengerStatus.WITHDRAWN,
                ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
                null,
                null,
                10L
            )
        );
        ChallengerDeactivationHistory history = saveChallengerDeactivationPort.saveHistory(
            ChallengerDeactivationHistory.create(
                deactivation,
                ChallengerDeactivationHistoryAction.REGISTERED,
                1,
                1,
                10L
            )
        );
        saveChallengerDeactivationPort.saveImages(List.of(
            ChallengerDeactivationImage.create(history, "image-id", 0)
        ));
        saveChallengerDeactivationPort.saveLinks(List.of(
            ChallengerDeactivationLink.create(history, "https://example.com", 0)
        ));
        doThrow(new IllegalStateException("진행 기록 삭제 실패"))
            .when(deleteChallengerDeactivationPort)
            .deleteHistoriesByDeactivationId(deactivation.getId());

        assertThatThrownBy(() -> deleteChallengerDeactivationDataUseCase.delete(deactivation.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("진행 기록 삭제 실패");

        assertThat(deactivationRepository.existsById(deactivation.getId())).isTrue();
        assertThat(historyRepository.count()).isOne();
        assertThat(imageRepository.count()).isOne();
        assertThat(linkRepository.count()).isOne();
    }

    @Test
    @DisplayName("스토리지 원본 삭제에 실패하면 탈부 기록과 근거 자료를 복구한다")
    void rollbackAllDataWhenStorageDeletionFails() {
        ChallengerDeactivation deactivation = saveChallengerDeactivationPort.save(
            ChallengerDeactivation.create(
                1L,
                ChallengerStatus.WITHDRAWN,
                ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
                null,
                null,
                10L
            )
        );
        ChallengerDeactivationHistory history = saveChallengerDeactivationPort.saveHistory(
            ChallengerDeactivationHistory.create(
                deactivation,
                ChallengerDeactivationHistoryAction.REGISTERED,
                1,
                0,
                10L
            )
        );
        saveChallengerDeactivationPort.saveImages(List.of(
            ChallengerDeactivationImage.create(history, "image-id", 0)
        ));
        fileMetadataRepository.saveAndFlush(fileMetadata("image-id"));
        doThrow(new IllegalStateException("스토리지 삭제 실패"))
            .when(storagePort).delete("private/challenger-deactivation/image-id.png");

        assertThatThrownBy(() -> deleteChallengerDeactivationDataUseCase.delete(deactivation.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("스토리지 삭제 실패");

        assertThat(deactivationRepository.existsById(deactivation.getId())).isTrue();
        assertThat(historyRepository.count()).isOne();
        assertThat(imageRepository.count()).isOne();
        assertThat(fileMetadataRepository.existsById("image-id")).isTrue();
    }

    @Test
    @DisplayName("일부 원본 삭제 후 롤백된 파일은 다른 탈부 기록에 다시 등록할 수 없다")
    void rejectReusingFileDeletedBeforeLaterStorageFailure() {
        ChallengerDeactivation expired = saveChallengerDeactivationPort.save(
            ChallengerDeactivation.create(
                1L,
                ChallengerStatus.WITHDRAWN,
                ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
                null,
                null,
                10L
            )
        );
        ChallengerDeactivationHistory expiredHistory = saveChallengerDeactivationPort.saveHistory(
            ChallengerDeactivationHistory.create(
                expired,
                ChallengerDeactivationHistoryAction.REGISTERED,
                2,
                0,
                10L
            )
        );
        saveChallengerDeactivationPort.saveImages(List.of(
            ChallengerDeactivationImage.create(expiredHistory, "a-image", 0),
            ChallengerDeactivationImage.create(expiredHistory, "b-image", 1)
        ));
        fileMetadataRepository.saveAllAndFlush(List.of(
            fileMetadata("a-image"),
            fileMetadata("b-image")
        ));

        ChallengerDeactivation other = saveChallengerDeactivationPort.save(
            ChallengerDeactivation.create(
                2L,
                ChallengerStatus.WITHDRAWN,
                ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
                null,
                null,
                10L
            )
        );
        saveChallengerDeactivationPort.saveHistory(ChallengerDeactivationHistory.create(
            other,
            ChallengerDeactivationHistoryAction.REGISTERED,
            0,
            0,
            10L
        ));
        doThrow(new IllegalStateException("두 번째 스토리지 삭제 실패"))
            .when(storagePort).delete("private/challenger-deactivation/b-image.png");

        assertThatThrownBy(() -> deleteChallengerDeactivationDataUseCase.delete(expired.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("두 번째 스토리지 삭제 실패");
        assertThat(imageRepository.count()).isEqualTo(2L);
        assertThat(fileMetadataRepository.existsById("a-image")).isTrue();

        AddChallengerDeactivationEvidenceCommand command = AddChallengerDeactivationEvidenceCommand.of(
            2L,
            List.of("a-image"),
            List.of(),
            10L
        );
        assertThatThrownBy(() -> addChallengerDeactivationEvidenceUseCase.addEvidence(command))
            .isInstanceOf(StorageException.class)
            .extracting("baseCode")
            .isEqualTo(StorageErrorCode.FILE_NOT_FOUND);
        assertThat(imageRepository.count()).isEqualTo(2L);
    }

    private FileMetadata fileMetadata(String fileId) {
        FileMetadata metadata = FileMetadata.builder()
            .fileId(fileId)
            .originalFileName("evidence.png")
            .category(FileCategory.CHALLENGER_DEACTIVATION_EVIDENCE)
            .contentType("image/png")
            .fileSize(100L)
            .storageProvider(StorageProvider.AWS_S3)
            .storageKey("private/challenger-deactivation/" + fileId + ".png")
            .uploadedMemberId(10L)
            .build();
        metadata.markAsUploaded();
        return metadata;
    }
}
