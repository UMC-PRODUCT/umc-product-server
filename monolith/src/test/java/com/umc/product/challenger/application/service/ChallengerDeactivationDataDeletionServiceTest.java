package com.umc.product.challenger.application.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.storage.application.port.in.command.DeleteStoredFileUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerDeactivationDataDeletionService")
class ChallengerDeactivationDataDeletionServiceTest {

    @Mock
    DeleteChallengerDeactivationPort deleteChallengerDeactivationPort;

    @Mock
    LoadChallengerDeactivationPort loadChallengerDeactivationPort;

    @Mock
    DeleteStoredFileUseCase deleteStoredFileUseCase;

    @InjectMocks
    ChallengerDeactivationDataDeletionService sut;

    @Test
    @DisplayName("외래 키 참조 순서에 맞춰 탈부 기록을 삭제한다")
    void deleteInForeignKeyOrder() {
        given(loadChallengerDeactivationPort.listImageIdsByDeactivationId(1L))
            .willReturn(List.of("image-1"));

        sut.delete(1L);

        InOrder order = inOrder(
            loadChallengerDeactivationPort,
            deleteStoredFileUseCase,
            deleteChallengerDeactivationPort
        );
        order.verify(loadChallengerDeactivationPort).getByIdForUpdate(1L);
        order.verify(loadChallengerDeactivationPort).listImageIdsByDeactivationId(1L);
        order.verify(deleteChallengerDeactivationPort).deleteImagesByDeactivationId(1L);
        order.verify(deleteChallengerDeactivationPort).deleteLinksByDeactivationId(1L);
        order.verify(deleteStoredFileUseCase).deleteIfNotReferencedOutsideDeactivation("image-1", 1L);
        order.verify(deleteChallengerDeactivationPort).deleteHistoriesByDeactivationId(1L);
        order.verify(deleteChallengerDeactivationPort).deleteById(1L);
    }
}
