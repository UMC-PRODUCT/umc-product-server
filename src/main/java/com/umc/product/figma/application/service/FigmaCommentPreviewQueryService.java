package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.in.PreviewFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.FigmaCommentPreviewInfo;
import com.umc.product.figma.application.port.out.FetchFigmaCommentPort;
import com.umc.product.figma.application.port.out.FetchFigmaFileMetadataPort;
import com.umc.product.figma.application.port.out.LoadFigmaPartRoutePort;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.figma.domain.FigmaPartRoute;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Discord 발송 / sync 상태 갱신 없이 신규 댓글과 매칭될 라우트만 조회하는 read-only 유즈케이스.
 * 운영진이 "지금 sync 하면 무엇이 어디로 갈까?" 를 미리 확인할 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FigmaCommentPreviewQueryService implements PreviewFigmaCommentsUseCase {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final LoadFigmaPartRoutePort loadFigmaPartRoutePort;
    private final FetchFigmaCommentPort fetchFigmaCommentPort;
    private final FetchFigmaFileMetadataPort fetchFigmaFileMetadataPort;
    private final FigmaIntegrationCommandService figmaIntegrationCommandService;

    @Override
    public FigmaCommentPreviewInfo preview(Long watchedFileId) {
        FigmaWatchedFile watchedFile = loadFigmaWatchedFilePort.findById(watchedFileId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_NOT_FOUND));

        String accessToken = figmaIntegrationCommandService.resolveActiveAccessToken();

        List<FigmaCommentInfo> comments = fetchFigmaCommentPort.listComments(watchedFile.getFileKey(), accessToken);
        List<FigmaCommentInfo> newComments = filterNewComments(comments, watchedFile.getLastSyncedCommentId());

        Map<String, String> nodeIdToPageName = resolvePageNames(watchedFile.getFileKey(), accessToken, newComments);
        List<FigmaPartRoute> routes = loadFigmaPartRoutePort.listByFileKey(watchedFile.getFileKey());
        Map<String, FigmaPartRoute> pageNameToRoute = routes.stream()
            .filter(route -> !route.isFallback())
            .collect(Collectors.toMap(FigmaPartRoute::getPageName, route -> route, (a, b) -> a));
        Optional<FigmaPartRoute> fallback = routes.stream().filter(FigmaPartRoute::isFallback).findFirst()
            .or(() -> loadFigmaPartRoutePort.findFallbackByFileKey(watchedFile.getFileKey()));

        List<FigmaCommentPreviewInfo.Item> items = new ArrayList<>(newComments.size());
        for (FigmaCommentInfo c : newComments) {
            String pageName = nodeIdToPageName.get(c.nodeId());
            FigmaPartRoute matched = pageName != null ? pageNameToRoute.get(pageName) : null;
            FigmaPartRoute applied = matched != null ? matched : fallback.orElse(null);

            items.add(new FigmaCommentPreviewInfo.Item(
                c.commentId(),
                c.message(),
                c.authorName(),
                c.nodeId(),
                pageName,
                applied != null ? applied.getPartKey() : null,
                applied != null ? applied.getDiscordRoleId() : null,
                applied != null ? applied.getDiscordWebhookUrl() : null,
                applied != null && applied.isFallback(),
                applied == null
            ));
        }

        return new FigmaCommentPreviewInfo(
            watchedFile.getFileKey(),
            watchedFile.getDisplayName(),
            watchedFile.getLastSyncedCommentId(),
            watchedFile.getLastSyncedAt(),
            items.size(),
            items
        );
    }

    private List<FigmaCommentInfo> filterNewComments(List<FigmaCommentInfo> comments, String lastSyncedCommentId) {
        if (lastSyncedCommentId == null) {
            return comments;
        }
        int boundary = -1;
        for (int i = 0; i < comments.size(); i++) {
            if (lastSyncedCommentId.equals(comments.get(i).commentId())) {
                boundary = i;
                break;
            }
        }
        if (boundary < 0) {
            return comments;
        }
        return comments.subList(boundary + 1, comments.size());
    }

    private Map<String, String> resolvePageNames(String fileKey, String accessToken, List<FigmaCommentInfo> comments) {
        Set<String> nodeIds = new HashSet<>();
        for (FigmaCommentInfo c : comments) {
            if (c.nodeId() != null) {
                nodeIds.add(c.nodeId());
            }
        }
        if (nodeIds.isEmpty()) {
            return Map.of();
        }
        try {
            return fetchFigmaFileMetadataPort.resolvePageNames(fileKey, accessToken, nodeIds);
        } catch (FigmaDomainException e) {
            log.warn("페이지명 해석 실패. 매칭은 fallback 으로 표시됩니다. fileKey={}", fileKey);
            return Map.of();
        }
    }
}
