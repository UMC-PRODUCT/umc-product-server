package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.BoardsResponse;
import com.umc.product.community.application.port.in.BoardsSearchQuery;
import com.umc.product.community.application.port.in.BoardsUseCase;
import com.umc.product.community.application.port.in.CreateBoardsCommand;
import com.umc.product.community.application.port.in.CreateLightningCommand;
import com.umc.product.community.application.port.in.UpdateBoardsCommand;
import com.umc.product.community.application.port.out.LoadBoardsPort;
import com.umc.product.community.application.port.out.SaveBoardsPort;
import com.umc.product.community.domain.Boards;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardsService implements BoardsUseCase {

    private final LoadBoardsPort loadBoardsPort;
    private final SaveBoardsPort saveBoardsPort;

    @Override
    public BoardsResponse createBoards(CreateBoardsCommand command) {
        Boards boards = Boards.createBoards(
                command.title(),
                command.content(),
                command.category(),
                command.region(),
                command.anonymous()
        );
        Boards saved = saveBoardsPort.save(boards);
        return BoardsResponse.from(saved);
    }

    @Override
    public BoardsResponse createLightningBoards(CreateLightningCommand command) {
        Boards.LightningInfo lightningInfo = new Boards.LightningInfo(
                command.meetAt(),
                command.location(),
                command.maxParticipants()
        );
        Boards boards = Boards.createLightning(
                command.title(),
                command.content(),
                command.region(),
                command.anonymous(),
                lightningInfo
        );
        Boards saved = saveBoardsPort.save(boards);
        return BoardsResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardsResponse> getBoards(BoardsSearchQuery query) {
        return loadBoardsPort.findAllByQuery(query).stream()
                .map(BoardsResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardsResponse getBoards(Long boardsId) {
        Boards boards = loadBoardsPort.findById(boardsId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + boardsId));
        return BoardsResponse.from(boards);
    }

    @Override
    public void deleteBoards(Long boardsId) {
        saveBoardsPort.deleteById(boardsId);
    }

    @Override
    public BoardsResponse updateBoards(Long boardsId, UpdateBoardsCommand command) {
        Boards boards = loadBoardsPort.findById(boardsId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + boardsId));
        boards.update(command.title(), command.content());
        saveBoardsPort.update(boards);
        return BoardsResponse.from(boards);
    }
}
