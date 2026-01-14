package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.application.port.in.post.Query.BoardsSearchQuery;
import com.umc.product.community.application.port.out.LoadBoardsPort;
import com.umc.product.community.application.port.out.SaveBoardsPort;
import com.umc.product.community.domain.Boards;
import com.umc.product.community.domain.Enum.Category;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardsPersistenceAdapter implements LoadBoardsPort, SaveBoardsPort {

    private final BoardsRepository boardsRepository;

    @Override
    public List<Boards> findAllByQuery(BoardsSearchQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size());
        return boardsRepository.findAll(pageable)
                .stream()
                .map(BoardsJpaEntity::toDomainWithId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boards> findById(Long id) {
        return boardsRepository.findById(id)
                .map(BoardsJpaEntity::toDomainWithId);
    }

    @Override
    public List<Boards> findByCategory(Category category) {
        return boardsRepository.findByCategory(category).stream()
                .map(BoardsJpaEntity::toDomainWithId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Boards> findByRegion(String region) {
        return boardsRepository.findByRegion(region).stream()
                .map(BoardsJpaEntity::toDomainWithId)
                .collect(Collectors.toList());
    }

    @Override
    public Boards save(Boards boards) {
        BoardsJpaEntity entity = BoardsJpaEntity.from(boards);
        BoardsJpaEntity saved = boardsRepository.save(entity);
        return saved.toDomainWithId();
    }

    @Override
    public void delete(Boards boards) {
        if (boards.getBoardsId() != null) {
            boardsRepository.deleteById(boards.getBoardsId().id());
        }
    }

    @Override
    public void update(Boards boards) {

    }

    @Override
    public void deleteById(Long id) {
        boardsRepository.deleteById(id);
    }
}
