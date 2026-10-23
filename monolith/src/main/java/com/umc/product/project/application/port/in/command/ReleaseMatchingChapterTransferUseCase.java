package com.umc.product.project.application.port.in.command;

import com.umc.product.project.application.port.in.command.dto.ReleaseMatchingChapterTransferCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingChapterTransferInfo;

public interface ReleaseMatchingChapterTransferUseCase {

    /**
     * 매칭 지부 이동을 해제해 챌린저를 원지부 매칭으로 되돌립니다.
     */
    ProjectMatchingChapterTransferInfo release(ReleaseMatchingChapterTransferCommand command);
}
