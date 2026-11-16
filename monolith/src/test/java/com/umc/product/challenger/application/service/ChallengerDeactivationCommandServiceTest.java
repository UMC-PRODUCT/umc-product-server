package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.challenger.application.port.in.command.dto.AddChallengerDeactivationEvidenceCommand;
import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.ChallengerDeactivationImage;
import com.umc.product.challenger.domain.ChallengerDeactivationLink;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationHistoryAction;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileMetadataInfo;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerDeactivationCommandService")
class ChallengerDeactivationCommandServiceTest {

    @Mock
    LoadChallengerPort loadChallengerPort;

    @Mock
    SaveChallengerPort saveChallengerPort;

    @Mock
    LoadChallengerDeactivationPort loadChallengerDeactivationPort;

    @Mock
    SaveChallengerDeactivationPort saveChallengerDeactivationPort;

    @Mock
    GetFileUseCase getFileUseCase;

    @Mock
    EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;

    @Captor
    ArgumentCaptor<ChallengerDeactivationHistory> historyCaptor;

    @Captor
    ArgumentCaptor<List<ChallengerDeactivationImage>> imagesCaptor;

    @Captor
    ArgumentCaptor<List<ChallengerDeactivationLink>> linksCaptor;

    @InjectMocks
    ChallengerDeactivationCommandService sut;

    @Test
    @DisplayName("탈부 정보와 근거 자료를 저장하고 챌린저 상태를 변경한다")
    void registerDeactivation() {
        Challenger challenger = challenger();
        RegisterChallengerDeactivationCommand command = command(
            ChallengerDeactivationReason.INVOLUNTARY_EVENT_NO_SHOW,
            List.of("image-1", "image-2"),
            List.of("https://example.com/evidence")
        );
        given(loadChallengerPort.getById(100L)).willReturn(challenger);
        given(getFileUseCase.batchGetUsableByIds(command.imageIds(), 10L)).willReturn(List.of(
            evidenceFile("image-1"),
            evidenceFile("image-2")
        ));
        given(saveChallengerDeactivationPort.save(any(ChallengerDeactivation.class)))
            .willAnswer(invocation -> {
                ChallengerDeactivation deactivation = invocation.getArgument(0);
                ReflectionTestUtils.setField(deactivation, "id", 500L);
                return deactivation;
            });

        Long result = sut.register(command);

        assertThat(result).isEqualTo(500L);
        assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.EXPELLED);
        assertThat(challenger.getModifiedBy()).isEqualTo(10L);
        assertThat(challenger.getModificationReason()).isEqualTo("INVOLUNTARY_EVENT_NO_SHOW");
        then(getFileUseCase).should().batchGetUsableByIds(command.imageIds(), 10L);
        then(saveChallengerDeactivationPort).should().saveHistory(historyCaptor.capture());
        then(saveChallengerDeactivationPort).should().saveImages(imagesCaptor.capture());
        then(saveChallengerDeactivationPort).should().saveLinks(linksCaptor.capture());
        assertThat(historyCaptor.getValue().getAction())
            .isEqualTo(ChallengerDeactivationHistoryAction.REGISTERED);
        assertThat(historyCaptor.getValue().getImageCount()).isEqualTo(2);
        assertThat(historyCaptor.getValue().getLinkCount()).isEqualTo(1);
        assertThat(imagesCaptor.getValue()).extracting(ChallengerDeactivationImage::getDisplayOrder)
            .containsExactly(0, 1);
        assertThat(linksCaptor.getValue()).extracting(ChallengerDeactivationLink::getDisplayOrder)
            .containsExactly(0);
        then(evictAuthoritySnapshotCacheUseCase).should().evictByMemberId(1L);
    }

    @Test
    @DisplayName("이미 탈부 등록된 챌린저는 다시 등록할 수 없다")
    void rejectAlreadyRegisteredDeactivation() {
        given(loadChallengerPort.getById(100L)).willReturn(challenger());
        given(loadChallengerDeactivationPort.existsByChallengerId(100L)).willReturn(true);

        assertThatThrownBy(() -> sut.register(command(
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            List.of(),
            List.of()
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.CHALLENGER_DEACTIVATION_ALREADY_EXISTS));

        then(saveChallengerDeactivationPort).should(never()).save(any());
    }

    @Test
    @DisplayName("같은 근거 자료를 중복 등록할 수 없다")
    void rejectDuplicateEvidence() {
        given(loadChallengerPort.getById(100L)).willReturn(challenger());

        assertThatThrownBy(() -> sut.register(command(
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            List.of("image-1", "image-1"),
            List.of()
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        then(getFileUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("탈부 근거 사진 카테고리가 아닌 파일은 등록할 수 없다")
    void rejectInvalidEvidenceFileCategory() {
        Challenger challenger = challenger();
        RegisterChallengerDeactivationCommand command = command(
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            List.of("image-1"),
            List.of()
        );
        given(loadChallengerPort.getById(100L)).willReturn(challenger);
        given(getFileUseCase.batchGetUsableByIds(command.imageIds(), 10L)).willReturn(List.of(
            new FileMetadataInfo(
                "image-1",
                "evidence.jpg",
                "jpg",
                FileCategory.POST_IMAGE,
                "image/jpeg",
                100L,
                true,
                10L,
                Instant.now()
            )
        ));

        assertThatThrownBy(() -> sut.register(command))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.ACTIVE);
        then(saveChallengerPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("탈부 사유가 없으면 챌린저 상태를 변경하지 않는다")
    void rejectMissingReasonBeforeChangingChallengerStatus() {
        Challenger challenger = challenger();
        RegisterChallengerDeactivationCommand command = command(null, List.of(), List.of());
        given(loadChallengerPort.getById(100L)).willReturn(challenger);

        assertThatThrownBy(() -> sut.register(command))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.ACTIVE);
        then(saveChallengerPort).shouldHaveNoInteractions();
        then(saveChallengerDeactivationPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("근거 사진을 사용할 수 없으면 챌린저 상태를 변경하지 않는다")
    void rejectUnusableEvidenceBeforeChangingChallengerStatus() {
        Challenger challenger = challenger();
        RegisterChallengerDeactivationCommand command = command(
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            List.of("image-1"),
            List.of()
        );
        given(loadChallengerPort.getById(100L)).willReturn(challenger);
        given(getFileUseCase.batchGetUsableByIds(command.imageIds(), 10L))
            .willThrow(new StorageException(StorageErrorCode.FILE_UPLOAD_NOT_COMPLETED));

        assertThatThrownBy(() -> sut.register(command))
            .isInstanceOf(StorageException.class)
            .satisfies(exception -> assertThat(((StorageException) exception).getBaseCode())
                .isEqualTo(StorageErrorCode.FILE_UPLOAD_NOT_COMPLETED));

        assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.ACTIVE);
        then(saveChallengerPort).shouldHaveNoInteractions();
        then(saveChallengerDeactivationPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기존 탈부 기록에 근거 자료와 근거 추가 진행 기록을 저장한다")
    void addEvidence() {
        ChallengerDeactivation deactivation = deactivation();
        AddChallengerDeactivationEvidenceCommand command = evidenceCommand(
            List.of(" image-2 ", "image-3"),
            List.of(" https://example.com/2 ")
        );
        given(loadChallengerDeactivationPort.getByChallengerIdForUpdate(100L))
            .willReturn(deactivation);
        given(loadChallengerDeactivationPort.countImagesByDeactivationId(500L)).willReturn(1L);
        given(loadChallengerDeactivationPort.countLinksByDeactivationId(500L)).willReturn(1L);
        given(getFileUseCase.batchGetUsableByIds(List.of("image-2", "image-3"), 10L))
            .willReturn(List.of(evidenceFile("image-2"), evidenceFile("image-3")));
        sut.addEvidence(command);

        then(saveChallengerDeactivationPort).should().saveHistory(historyCaptor.capture());
        then(saveChallengerDeactivationPort).should().saveImages(imagesCaptor.capture());
        then(saveChallengerDeactivationPort).should().saveLinks(linksCaptor.capture());
        assertThat(historyCaptor.getValue().getAction())
            .isEqualTo(ChallengerDeactivationHistoryAction.EVIDENCE_ADDED);
        assertThat(historyCaptor.getValue().getImageCount()).isEqualTo(2);
        assertThat(historyCaptor.getValue().getLinkCount()).isEqualTo(1);
        assertThat(historyCaptor.getValue().getActorMemberId()).isEqualTo(10L);
        assertThat(imagesCaptor.getValue()).extracting(ChallengerDeactivationImage::getDisplayOrder)
            .containsExactly(1, 2);
        assertThat(linksCaptor.getValue()).extracting(ChallengerDeactivationLink::getDisplayOrder)
            .containsExactly(1);
    }

    @Test
    @DisplayName("사진 없이 링크만 근거 자료로 추가할 수 있다")
    void addLinkEvidenceWithoutImages() {
        ChallengerDeactivation deactivation = deactivation();
        given(loadChallengerDeactivationPort.getByChallengerIdForUpdate(100L))
            .willReturn(deactivation);
        given(getFileUseCase.batchGetUsableByIds(List.of(), 10L)).willReturn(List.of());

        sut.addEvidence(evidenceCommand(
            null,
            List.of("https://example.com/link-only")
        ));

        then(saveChallengerDeactivationPort).should().saveHistory(historyCaptor.capture());
        then(saveChallengerDeactivationPort).should().saveImages(List.of());
        then(saveChallengerDeactivationPort).should().saveLinks(linksCaptor.capture());
        assertThat(historyCaptor.getValue().getImageCount()).isZero();
        assertThat(historyCaptor.getValue().getLinkCount()).isEqualTo(1);
        assertThat(linksCaptor.getValue()).extracting(ChallengerDeactivationLink::getLink)
            .containsExactly("https://example.com/link-only");
    }

    @Test
    @DisplayName("근거 추가에서도 탈부 근거 사진 카테고리가 아닌 파일은 등록할 수 없다")
    void rejectInvalidEvidenceFileCategoryWhenAddingEvidence() {
        given(loadChallengerDeactivationPort.getByChallengerIdForUpdate(100L))
            .willReturn(deactivation());
        given(getFileUseCase.batchGetUsableByIds(List.of("image-2"), 10L)).willReturn(List.of(
            new FileMetadataInfo(
                "image-2",
                "evidence.jpg",
                "jpg",
                FileCategory.POST_IMAGE,
                "image/jpeg",
                100L,
                true,
                10L,
                Instant.now()
            )
        ));

        assertThatThrownBy(() -> sut.addEvidence(evidenceCommand(
            List.of("image-2"),
            List.of()
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        then(saveChallengerDeactivationPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기존 자료와 합쳐 사진 5장 또는 링크 3개를 초과할 수 없다")
    void rejectEvidenceCapacityExceeded() {
        ChallengerDeactivation deactivation = deactivation();
        given(loadChallengerDeactivationPort.getByChallengerIdForUpdate(100L))
            .willReturn(deactivation);
        given(loadChallengerDeactivationPort.countImagesByDeactivationId(500L)).willReturn(4L);
        given(loadChallengerDeactivationPort.countLinksByDeactivationId(500L)).willReturn(3L);

        assertThatThrownBy(() -> sut.addEvidence(evidenceCommand(
            List.of("image-5", "image-6"),
            List.of()
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        then(getFileUseCase).shouldHaveNoInteractions();
        then(saveChallengerDeactivationPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기존 링크와 합쳐 3개를 초과할 수 없다")
    void rejectLinkCapacityExceeded() {
        ChallengerDeactivation deactivation = deactivation();
        given(loadChallengerDeactivationPort.getByChallengerIdForUpdate(100L))
            .willReturn(deactivation);
        given(loadChallengerDeactivationPort.countLinksByDeactivationId(500L)).willReturn(2L);

        assertThatThrownBy(() -> sut.addEvidence(evidenceCommand(
            List.of(),
            List.of("https://example.com/3", "https://example.com/4")
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        then(getFileUseCase).shouldHaveNoInteractions();
        then(saveChallengerDeactivationPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기존 탈부 기록에 등록된 근거 자료를 다시 추가할 수 없다")
    void rejectAlreadyRegisteredEvidence() {
        ChallengerDeactivation deactivation = deactivation();
        given(loadChallengerDeactivationPort.getByChallengerIdForUpdate(100L))
            .willReturn(deactivation);
        given(loadChallengerDeactivationPort.existsAnyImageByDeactivationId(
            500L,
            List.of("image-1")
        )).willReturn(true);

        assertThatThrownBy(() -> sut.addEvidence(evidenceCommand(
            List.of("image-1"),
            List.of()
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        then(getFileUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기존 탈부 기록에 등록된 링크를 다시 추가할 수 없다")
    void rejectAlreadyRegisteredLinkEvidence() {
        ChallengerDeactivation deactivation = deactivation();
        String existingLink = "https://example.com/existing";
        given(loadChallengerDeactivationPort.getByChallengerIdForUpdate(100L))
            .willReturn(deactivation);
        given(loadChallengerDeactivationPort.existsAnyLinkByDeactivationId(
            500L,
            List.of(existingLink)
        )).willReturn(true);

        assertThatThrownBy(() -> sut.addEvidence(evidenceCommand(
            List.of(),
            List.of(existingLink)
        )))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        then(getFileUseCase).shouldHaveNoInteractions();
        then(saveChallengerDeactivationPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("사진과 링크가 모두 없으면 근거 추가 기록을 만들 수 없다")
    void rejectEmptyEvidenceAddition() {
        given(loadChallengerDeactivationPort.getByChallengerIdForUpdate(100L))
            .willReturn(deactivation());

        assertThatThrownBy(() -> sut.addEvidence(evidenceCommand(List.of(), List.of())))
            .isInstanceOf(ChallengerDomainException.class)
            .satisfies(exception -> assertThat(((ChallengerDomainException) exception).getBaseCode())
                .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST));

        then(getFileUseCase).shouldHaveNoInteractions();
        then(saveChallengerDeactivationPort).shouldHaveNoInteractions();
    }

    private Challenger challenger() {
        Challenger challenger = Challenger.builder()
            .memberId(1L)
            .gisuId(11L)
            .part(ChallengerPart.SPRINGBOOT)
            .build();
        ReflectionTestUtils.setField(challenger, "id", 100L);
        return challenger;
    }

    private ChallengerDeactivation deactivation() {
        ChallengerDeactivation deactivation = ChallengerDeactivation.create(
            100L,
            ChallengerStatus.WITHDRAWN,
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            null,
            null,
            10L
        );
        ReflectionTestUtils.setField(deactivation, "id", 500L);
        return deactivation;
    }

    private AddChallengerDeactivationEvidenceCommand evidenceCommand(
        List<String> imageIds,
        List<String> links
    ) {
        return AddChallengerDeactivationEvidenceCommand.of(100L, imageIds, links, 10L);
    }

    private RegisterChallengerDeactivationCommand command(
        ChallengerDeactivationReason reason,
        List<String> imageIds,
        List<String> links
    ) {
        return RegisterChallengerDeactivationCommand.of(
            100L,
            reason,
            null,
            "운영진 메모",
            imageIds,
            links,
            10L
        );
    }

    private FileMetadataInfo evidenceFile(String fileId) {
        return new FileMetadataInfo(
            fileId,
            fileId + ".jpg",
            "jpg",
            FileCategory.CHALLENGER_DEACTIVATION_EVIDENCE,
            "image/jpeg",
            100L,
            true,
            10L,
            Instant.now()
        );
    }
}
