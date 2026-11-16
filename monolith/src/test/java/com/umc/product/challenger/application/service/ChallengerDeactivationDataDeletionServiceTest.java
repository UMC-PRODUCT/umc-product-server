package com.umc.product.challenger.application.service;

import static org.mockito.Mockito.inOrder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerDeactivationDataDeletionService")
class ChallengerDeactivationDataDeletionServiceTest {

    @Mock
    DeleteChallengerDeactivationPort deleteChallengerDeactivationPort;

    @InjectMocks
    ChallengerDeactivationDataDeletionService sut;

    @Test
    @DisplayName("외래 키 참조 순서에 맞춰 탈부 기록을 삭제한다")
    void deleteInForeignKeyOrder() {
        sut.delete(1L);

        InOrder order = inOrder(deleteChallengerDeactivationPort);
        order.verify(deleteChallengerDeactivationPort).deleteImagesByDeactivationId(1L);
        order.verify(deleteChallengerDeactivationPort).deleteLinksByDeactivationId(1L);
        order.verify(deleteChallengerDeactivationPort).deleteHistoriesByDeactivationId(1L);
        order.verify(deleteChallengerDeactivationPort).deleteById(1L);
    }
}
