package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.in.PreviewFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.FigmaCommentPreviewInfo;
import com.umc.product.figma.application.port.out.FetchFigmaCommentPort;
import com.umc.product.figma.application.port.out.FetchFigmaFileMetadataPort;
import com.umc.product.figma.application.port.out.LoadFigmaRoutingDomainPort;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
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
 * Discord 발송 / sync 상태 갱신 없이 신규 댓글과 LLM 분류 결과를 반환하는 read-only 유즈케이스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FigmaCommentPreviewQueryService implements PreviewFigmaCommentsUseCase {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final LoadFigmaRoutingDomainPort loadFigmaRoutingDomainPort;
    private final FetchFigmaCommentPort fetchFigmaCommentPort;
    private final FetchFigmaFileMetadataPort fetchFigmaFileMetadataPort;
    private final FigmaIntegrationCommandService figmaIntegrationCommandService;
    private final FigmaCommentDomainClassifier figmaCommentDomainClassifier;

    @Override
    public FigmaCommentPreviewInfo preview(Long watchedFileId) {
        FigmaWatchedFile watchedFile = loadFigmaWatchedFilePort.findById(watchedFileId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_NOT_FOUND));

        String accessToken = figmaIntegrationCommandService.resolveActiveAccessToken();

        List<FigmaCommentInfo> comments = fetchFigmaCommentPort.listComments(watchedFile.getFileKey(), accessToken);
        List<FigmaCommentInfo> newComments = filterNewComments(comments, watchedFile.getLastSyncedCommentId());

        Map<String, String> nodeIdToPageName = resolvePageNames(watchedFile.getFileKey(), accessToken, newComments);
        List<FigmaRoutingDomain> domains = loadFigmaRoutingDomainPort.listAllDomains();
        Map<String, FigmaRoutingDomain> domainByKey = domains.stream()
            .collect(Collectors.toMap(FigmaRoutingDomain::getDomainKey, d -> d, (a, b) -> a));
        Optional<FigmaRoutingDomain> fallback = domains.stream()
            .filter(FigmaRoutingDomain::isFallback)
            .findFirst()
            .or(loadFigmaRoutingDomainPort::findFallbackDomain);
        List<String> candidateKeys = domains.stream().map(FigmaRoutingDomain::getDomainKey).toList();

        List<FigmaCommentPreviewInfo.Item> items = new ArrayList<>(newComments.size());
        for (FigmaCommentInfo c : newComments) {
            String pageName = c.nodeId() != null ? nodeIdToPageName.get(c.nodeId()) : null;
            String classified = candidateKeys.isEmpty() ? null : figmaCommentDomainClassifier.classify(c, candidateKeys);
            FigmaRoutingDomain matched = classified != null ? domainByKey.get(classified) : null;
            FigmaRoutingDomain applied = matched != null ? matched : fallback.orElse(null);

            List<String> mentionRenders = applied != null
                ? loadFigmaRoutingDomainPort.listMentionsByDomainId(applied.getId()).stream()
                    .map(FigmaRoutingDomainMention::render).toList()
                : List.of();

            items.add(new FigmaCommentPreviewInfo.Item(
                c.commentId(),
                c.message(),
                c.authorName(),
                c.nodeId(),
                pageName,
                classified,
                applied != null ? applied.getDomainKey() : null,
                applied != null ? applied.getDiscordWebhookUrl() : null,
                mentionRenders,
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
            log.warn("페이지명 해석 실패. preview 의 pageName 필드는 비워집니다. fileKey={}", fileKey);
            return Map.of();
        }
    }
}
