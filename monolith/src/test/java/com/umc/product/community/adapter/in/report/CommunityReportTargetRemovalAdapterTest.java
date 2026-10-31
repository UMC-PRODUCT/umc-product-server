package com.umc.product.community.adapter.in.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.community.application.port.in.command.thread.message.ModerateCommunityThreadMessageUseCase;
import com.umc.product.community.application.port.in.command.thread.message.dto.CommunityThreadMessageRemovalResult;
import com.umc.product.report.domain.enums.ReportTargetRemoval;

@ExtendWith(MockitoExtension.class)
@DisplayName("신고 대상 삭제 어댑터")
class CommunityReportTargetRemovalAdapterTest {

    private static final Long THREAD_ID = 10L;
    private static final Long MESSAGE_ID = 20L;

    @Mock
    ModerateCommunityThreadMessageUseCase moderateCommunityThreadMessageUseCase;

    @InjectMocks
    CommunityReportTargetRemovalAdapter sut;

    @Test
    @DisplayName("community 삭제 결과를 신고 삭제 결과로 바꿔서 돌려준다")
    void removeThreadMessage_결과를_매핑한다() {
        // given
        given(moderateCommunityThreadMessageUseCase.removeByOperator(THREAD_ID, MESSAGE_ID))
            .willReturn(CommunityThreadMessageRemovalResult.DELETED)
            .willReturn(CommunityThreadMessageRemovalResult.ALREADY_DELETED)
            .willReturn(CommunityThreadMessageRemovalResult.NOT_FOUND);

        // when & then
        assertThat(sut.removeThreadMessage(THREAD_ID, MESSAGE_ID)).isEqualTo(ReportTargetRemoval.DELETED);
        assertThat(sut.removeThreadMessage(THREAD_ID, MESSAGE_ID)).isEqualTo(ReportTargetRemoval.ALREADY_DELETED);
        assertThat(sut.removeThreadMessage(THREAD_ID, MESSAGE_ID)).isEqualTo(ReportTargetRemoval.NOT_FOUND);
    }
}
