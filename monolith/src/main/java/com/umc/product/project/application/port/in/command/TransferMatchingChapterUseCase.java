package com.umc.product.project.application.port.in.command;

import java.util.List;

import com.umc.product.project.application.port.in.command.dto.TransferMatchingChapterCommand;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingChapterTransferInfo;

public interface TransferMatchingChapterUseCase {

    /**
     * 챌린저들의 매칭 지부 이동을 일괄 등록합니다. 한 명이라도 검증에 실패하면 전체가 롤백됩니다.
     */
    List<ProjectMatchingChapterTransferInfo> transfer(TransferMatchingChapterCommand command);
}
