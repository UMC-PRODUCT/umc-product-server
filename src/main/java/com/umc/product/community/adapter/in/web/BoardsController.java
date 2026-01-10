package com.umc.product.community.adapter.in.web;

import com.umc.product.community.application.port.in.BoardsResponse;
import com.umc.product.community.application.port.in.BoardsSearchQuery;
import com.umc.product.community.application.port.in.BoardsUseCase;
import com.umc.product.community.application.port.in.CreateBoardsCommand;
import com.umc.product.community.domain.BoardsSortType;
import com.umc.product.community.domain.Category;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
@Tag(name = "Boards", description = "게시글 API")
public class BoardsController {

    private final BoardsUseCase boardsUseCase;

    @PostMapping
    public ResponseEntity<BoardsResponse> createBoards(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam String region,
            @RequestParam(defaultValue = "false") boolean anonymous) {

        CreateBoardsCommand command = new CreateBoardsCommand(
                title,
                content,
                Category.valueOf(category),
                region,
                anonymous
        );

        BoardsResponse response = boardsUseCase.createBoards(command);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BoardsResponse>> getBoards(
            @RequestParam(defaultValue = "true") boolean ing,
            @RequestParam(defaultValue = "ALL") BoardsSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        BoardsSearchQuery query = new BoardsSearchQuery(ing, sort, page, size);
        List<BoardsResponse> boards = boardsUseCase.getBoards(query);

        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{boardsId}")
    public BoardsResponse getBoards(@PathVariable Long boardsId) {
        return boardsUseCase.getBoards(boardsId);
    }

}
