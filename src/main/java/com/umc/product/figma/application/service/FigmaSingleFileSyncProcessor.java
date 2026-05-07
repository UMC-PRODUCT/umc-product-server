package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.out.FetchFigmaCommentPort;
import com.umc.product.figma.application.port.out.FetchFigmaFileMetadataPort;
import com.umc.product.figma.application.port.out.LoadFigmaPartRoutePort;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SaveFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SendDiscordMentionPort;
import com.umc.product.figma.application.port.out.dto.DiscordMentionMessage;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.figma.domain.FigmaPartRoute;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일 1건의 동기화를 별도 트랜잭션으로 수행한다.
 * 별도 빈으로 분리된 이유는 Spring의 self-invocation 한계를 피하기 위함이며,
 * 한 파일에서 발생한 실패가 다른 파일의 sync 상태에 영향을 주지 않게 하기 위함이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaSingleFileSyncProcessor {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final SaveFigmaWatchedFilePort saveFigmaWatchedFilePort;
    private final LoadFigmaPartRoutePort loadFigmaPartRoutePort;
    private final FetchFigmaCommentPort fetchFigmaCommentPort;
    private final FetchFigmaFileMetadataPort fetchFigmaFileMetadataPort;
    private final SendDiscordMentionPort sendDiscordMentionPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long watchedFileId, String accessToken) {
        FigmaWatchedFile watchedFile = loadFigmaWatchedFilePort.findById(watchedFileId)
            .orElse(null);
        if (watchedFile == null) {
            return;
        }

        try {
            List<FigmaCommentInfo> comments = fetchFigmaCommentPort.listComments(watchedFile.getFileKey(), accessToken);
            List<FigmaCommentInfo> newComments = filterNewComments(comments, watchedFile.getLastSyncedCommentId());
            if (newComments.isEmpty()) {
                watchedFile.markSynced(watchedFile.getLastSyncedCommentId(), Instant.now());
                saveFigmaWatchedFilePort.save(watchedFile);
                return;
            }

            Map<String, String> nodeIdToPageName = resolvePageNames(watchedFile.getFileKey(), accessToken, newComments);
            List<FigmaPartRoute> routes = loadFigmaPartRoutePort.listByFileKey(watchedFile.getFileKey());
            Map<String, FigmaPartRoute> pageNameToRoute = routes.stream()
                .filter(route -> !route.isFallback())
                .collect(Collectors.toMap(FigmaPartRoute::getPageName, route -> route, (a, b) -> a));
            Optional<FigmaPartRoute> fallback = routes.stream().filter(FigmaPartRoute::isFallback).findFirst()
                .or(() -> loadFigmaPartRoutePort.findFallbackByFileKey(watchedFile.getFileKey()));

            for (FigmaCommentInfo comment : newComments) {
                FigmaPartRoute route = resolveRoute(comment, nodeIdToPageName, pageNameToRoute, fallback);
                if (route == null) {
                    log.warn("Figma 댓글에 대한 라우트가 없습니다. fileKey={}, commentId={}",
                        watchedFile.getFileKey(), comment.commentId());
                    continue;
                }
                String pageName = comment.nodeId() != null ? nodeIdToPageName.get(comment.nodeId()) : null;
                trySendMention(watchedFile, comment, pageName, route);
            }

            FigmaCommentInfo last = newComments.get(newComments.size() - 1);
            watchedFile.markSynced(last.commentId(), Instant.now());
            saveFigmaWatchedFilePort.save(watchedFile);
        } catch (FigmaDomainException e) {
            log.warn("Figma 동기화 실패: fileKey={}, message={}", watchedFile.getFileKey(), e.getMessage());
            watchedFile.recordError(e.getMessage());
            saveFigmaWatchedFilePort.save(watchedFile);
        }
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
            log.warn("페이지명 해석 실패. 라우팅은 fallback 으로 처리됩니다. fileKey={}", fileKey);
            return new HashMap<>();
        }
    }

    private FigmaPartRoute resolveRoute(
        FigmaCommentInfo comment,
        Map<String, String> nodeIdToPageName,
        Map<String, FigmaPartRoute> pageNameToRoute,
        Optional<FigmaPartRoute> fallback
    ) {
        String pageName = comment.nodeId() != null ? nodeIdToPageName.get(comment.nodeId()) : null;
        if (pageName != null) {
            FigmaPartRoute matched = pageNameToRoute.get(pageName);
            if (matched != null) {
                return matched;
            }
        }
        return fallback.orElse(null);
    }

    private void trySendMention(
        FigmaWatchedFile watchedFile,
        FigmaCommentInfo comment,
        String pageName,
        FigmaPartRoute route
    ) {
        try {
            String content = renderMessage(watchedFile, comment, pageName, route.getDiscordRoleId());
            sendDiscordMentionPort.send(new DiscordMentionMessage(
                route.getDiscordWebhookUrl(),
                route.getDiscordRoleId(),
                content
            ));
        } catch (FigmaDomainException e) {
            log.warn("Discord 멘션 전송 실패. 다음 폴링에서 재발송하지 않습니다. fileKey={}, commentId={}",
                watchedFile.getFileKey(), comment.commentId());
        }
    }

    private String renderMessage(FigmaWatchedFile watchedFile, FigmaCommentInfo comment, String pageName, String roleId) {
        String displayedPageName = pageName != null ? pageName : "(unmapped)";
        String link = String.format(
            "https://www.figma.com/file/%s?node-id=%s#%s",
            watchedFile.getFileKey(),
            comment.nodeId() == null ? "" : comment.nodeId(),
            comment.commentId()
        );

        return String.format(
            "<@&%s> [Figma] %s / %s%n👤 %s%n💬 %s%n🔗 %s",
            roleId,
            watchedFile.getDisplayName(),
            displayedPageName,
            comment.authorName(),
            comment.message(),
            link
        );
    }
}
