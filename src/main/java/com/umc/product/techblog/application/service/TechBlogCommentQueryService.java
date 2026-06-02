package com.umc.product.techblog.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.techblog.application.port.in.query.GetTechBlogCommentListUseCase;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentCursorInfo;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentInfo;
import com.umc.product.techblog.application.port.in.query.dto.TechBlogCommentListQuery;
import com.umc.product.techblog.application.port.out.LoadTechBlogCommentPort;
import com.umc.product.techblog.application.port.out.LoadTechBlogContentPort;
import com.umc.product.techblog.domain.TechBlogComment;
import com.umc.product.techblog.domain.TechBlogCommentSort;
import com.umc.product.techblog.domain.TechBlogContent;
import com.umc.product.techblog.domain.TechBlogContentType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechBlogCommentQueryService implements GetTechBlogCommentListUseCase {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final LoadTechBlogContentPort loadTechBlogContentPort;
    private final LoadTechBlogCommentPort loadTechBlogCommentPort;
    private final TechBlogCommentInfoAssembler commentInfoAssembler;

    @Override
    public TechBlogCommentCursorInfo getComments(TechBlogCommentListQuery query) {
        TechBlogContentType type = TechBlogContentType.fromPath(query.type());
        return loadTechBlogContentPort.findByTypeAndSlug(type, query.slug())
            .map(content -> getComments(content, query))
            .orElseGet(() -> new TechBlogCommentCursorInfo(List.of(), null, false));
    }

    private TechBlogCommentCursorInfo getComments(TechBlogContent content, TechBlogCommentListQuery query) {
        int size = normalizeSize(query.size());
        TechBlogCommentSort sort = TechBlogCommentSort.from(query.sort());

        List<TechBlogComment> rows = loadTechBlogCommentPort.listTopLevel(
            content.getId(),
            sort,
            query.cursor(),
            size + 1
        );

        boolean hasNext = rows.size() > size;
        List<TechBlogComment> page = hasNext ? rows.subList(0, size) : rows;
        if (page.isEmpty()) {
            return new TechBlogCommentCursorInfo(List.of(), null, false);
        }

        List<Long> parentIds = page.stream().map(TechBlogComment::getId).toList();
        Map<Long, List<TechBlogComment>> repliesByParentId = loadTechBlogCommentPort
            .listRepliesByParentIds(parentIds)
            .stream()
            .collect(Collectors.groupingBy(TechBlogComment::getParentCommentId));

        List<Long> allCommentIds = page.stream()
            .flatMap(comment -> {
                List<TechBlogComment> replies = repliesByParentId.getOrDefault(comment.getId(), List.of());
                return java.util.stream.Stream.concat(java.util.stream.Stream.of(comment), replies.stream());
            })
            .map(TechBlogComment::getId)
            .toList();

        Map<Long, Integer> likeCounts = loadTechBlogCommentPort.countLikesByCommentIds(allCommentIds);
        Set<Long> likedCommentIds = loadTechBlogCommentPort.findLikedCommentIds(allCommentIds, query.viewerMemberId());
        List<TechBlogCommentInfo> comments = commentInfoAssembler.assembleTree(
            page,
            repliesByParentId,
            likeCounts,
            likedCommentIds
        );

        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
        return new TechBlogCommentCursorInfo(comments, nextCursor, hasNext);
    }

    private int normalizeSize(int requestedSize) {
        if (requestedSize <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(requestedSize, MAX_SIZE);
    }
}
