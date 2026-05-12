package com.umc.product.figma.application.service;

import com.umc.product.figma.config.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.SummarizeFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.FigmaSummaryResult;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;
import com.umc.product.figma.application.port.out.FetchFigmaCommentPort;
import com.umc.product.figma.application.port.out.FetchFigmaFileMetadataPort;
import com.umc.product.figma.application.port.out.LoadFigmaCommentDispatchPort;
import com.umc.product.figma.application.port.out.LoadFigmaRoutingDomainPort;
import com.umc.product.figma.application.port.out.LoadFigmaSummaryCursorPort;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SaveFigmaCommentDispatchPort;
import com.umc.product.figma.application.port.out.SaveFigmaSummaryCursorPort;
import com.umc.product.figma.application.port.out.SendDiscordMentionPort;
import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage;
import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage.CommentEntry;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
import com.umc.product.figma.domain.FigmaSummaryCursor;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * 시간창 기반 figma 댓글 동기화의 단일 본체 (ADR-004 §Decision 1·2).
 * <p>
 * sync / digest / preview 세 진입점이 본 service 의 같은 메서드를 호출하며, 차이는 {@link SummarizeFigmaCommentsCommand} 의 dryRun / force /
 * advanceCursor 플래그로 표현된다. ADR-003 amendment 시점의 FigmaCommentBatchProcessor 의 Mode 이중 분기를 단일 경로로 축소한 형태다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaCommentSummaryService implements SummarizeFigmaCommentsUseCase {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final LoadFigmaRoutingDomainPort loadFigmaRoutingDomainPort;
    private final FetchFigmaCommentPort fetchFigmaCommentPort;
    private final FetchFigmaFileMetadataPort fetchFigmaFileMetadataPort;
    private final SendDiscordMentionPort sendDiscordMentionPort;
    private final LoadFigmaCommentDispatchPort loadFigmaCommentDispatchPort;
    private final SaveFigmaCommentDispatchPort saveFigmaCommentDispatchPort;
    private final LoadFigmaSummaryCursorPort loadFigmaSummaryCursorPort;
    private final SaveFigmaSummaryCursorPort saveFigmaSummaryCursorPort;
    private final FigmaIntegrationCommandService figmaIntegrationCommandService;
    private final FigmaCommentDomainClassifier figmaCommentDomainClassifier;
    private final FigmaWatchedFileStateUpdater figmaWatchedFileStateUpdater;
    private final FigmaSyncProperties figmaSyncProperties;

    @Override
    @Transactional
    public FigmaSummaryResult summarize(SummarizeFigmaCommentsCommand command) {
        if (command == null) {
            throw new FigmaDomainException(FigmaErrorCode.DIGEST_RANGE_INVALID, "command 가 null 입니다.");
        }
        if (command.from() != null && command.to() != null && command.from().isAfter(command.to())) {
            throw new FigmaDomainException(FigmaErrorCode.DIGEST_RANGE_INVALID, "from 이 to 보다 이후일 수 없습니다.");
        }

        List<FigmaWatchedFile> files = resolveTargetFiles(command);
        if (files.isEmpty()) {
            return FigmaSummaryResult.empty(command.from(), command.to());
        }

        String accessToken;
        try {
            accessToken = figmaIntegrationCommandService.resolveActiveAccessToken();
        } catch (FigmaDomainException e) {
            log.warn("Figma access token 확보 실패. summarize 를 건너뜁니다: {}", e.getMessage());
            return FigmaSummaryResult.empty(command.from(), command.to());
        }

        List<FigmaRoutingDomain> domains = loadFigmaRoutingDomainPort.listAllDomains();
        if (domains.isEmpty()) {
            throw new FigmaDomainException(FigmaErrorCode.ROUTING_DOMAIN_NOT_REGISTERED);
        }
        Map<String, FigmaRoutingDomain> domainByKey = domains.stream()
            .collect(Collectors.toMap(FigmaRoutingDomain::getDomainKey, d -> d, (a, b) -> a));
        Map<Long, FigmaRoutingDomain> domainById = domains.stream()
            .collect(Collectors.toMap(FigmaRoutingDomain::getId, d -> d));
        Optional<FigmaRoutingDomain> fallback = domains.stream()
            .filter(FigmaRoutingDomain::isFallback)
            .findFirst()
            .or(loadFigmaRoutingDomainPort::findFallbackDomain);
        List<String> candidateKeys = domains.stream().map(FigmaRoutingDomain::getDomainKey).toList();

        Map<Long, List<EnrichedComment>> commentsByDomainId = new LinkedHashMap<>();
        Set<Long> erroredFileIds = new HashSet<>();
        int totalComments = 0;
        int unmatchedCount = 0;

        for (FigmaWatchedFile file : files) {
            try {
                List<FigmaCommentInfo> comments = fetchFigmaCommentPort.listComments(file.getFileKey(), accessToken);
                List<FigmaCommentInfo> filtered = filterByWindow(comments, command.from(), command.to());
                if (filtered.isEmpty()) {
                    continue;
                }
                Map<String, String> nodeIdToPageName = resolvePageNames(file.getFileKey(), accessToken, filtered);
                Map<String, String> classifications =
                    figmaCommentDomainClassifier.classifyBatch(filtered, candidateKeys);

                for (FigmaCommentInfo c : filtered) {
                    String classified = classifications.get(c.commentId());
                    FigmaRoutingDomain matched = classified != null ? domainByKey.get(classified) : null;
                    FigmaRoutingDomain applied = matched != null ? matched : fallback.orElse(null);
                    if (applied == null) {
                        unmatchedCount++;
                        log.warn("매칭된 라우팅 도메인이 없고 fallback 도 없습니다. fileKey={}, commentId={}",
                            file.getFileKey(), c.commentId());
                        continue;
                    }
                    String pageName = c.nodeId() != null ? nodeIdToPageName.get(c.nodeId()) : null;
                    commentsByDomainId
                        .computeIfAbsent(applied.getId(), k -> new ArrayList<>())
                        .add(new EnrichedComment(file, c, pageName, classified));
                    totalComments++;
                }
            } catch (FigmaDomainException e) {
                log.warn("Figma 댓글 수집 실패: fileKey={}, message={}", file.getFileKey(), e.getMessage());
                erroredFileIds.add(file.getId());
                if (!command.dryRun()) {
                    figmaWatchedFileStateUpdater.recordError(file.getId(), e.getMessage());
                }
            }
        }

        // dispatch 조회는 항상 수행해 응답의 alreadyDispatched 플래그를 채운다.
        // 발송 단계에서 실제로 제외할지는 force / dryRun 플래그에 따라 다르다.
        Set<String> alreadyDispatched = commentsByDomainId.isEmpty()
            ? Set.of()
            : loadFigmaCommentDispatchPort.findDispatchedCommentIds(allCommentIds(commentsByDomainId));

        // N+1 방지: 도메인 루프 전에 모든 도메인의 mentions 를 한 번에 일괄 조회
        Map<Long, List<FigmaRoutingDomainMention>> mentionsByDomainId =
            loadFigmaRoutingDomainPort.listMentionsByDomainIds(commentsByDomainId.keySet());

        int skippedAlreadyDispatched = 0;
        List<FigmaSummaryResult.DomainGroup> domainGroups = new ArrayList<>();
        for (Map.Entry<Long, List<EnrichedComment>> entry : commentsByDomainId.entrySet()) {
            FigmaRoutingDomain domain = domainById.get(entry.getKey());
            if (domain == null) {
                continue;
            }
            List<FigmaRoutingDomainMention> mentions =
                mentionsByDomainId.getOrDefault(domain.getId(), List.of());
            List<EnrichedComment> bucket = entry.getValue();
            List<EnrichedComment> sendable = new ArrayList<>(bucket.size());
            for (EnrichedComment ec : bucket) {
                boolean isDispatched = alreadyDispatched.contains(ec.comment.commentId());
                // force=true 면 dispatch 무관하게 발송 대상에 포함 (catch-up 시맨틱)
                if (isDispatched && !command.force()) {
                    skippedAlreadyDispatched++;
                } else {
                    sendable.add(ec);
                }
            }

            List<String> dispatchedIds = List.of();
            if (!command.dryRun() && !sendable.isEmpty()) {
                dispatchedIds = sendDomainBatch(domain, mentions, sendable, command.from(), command.to());
                if (!dispatchedIds.isEmpty()) {
                    saveFigmaCommentDispatchPort.recordDispatched(
                        dispatchedIds,
                        domain.getId(),
                        Instant.now()
                    );
                }
            }
            domainGroups.add(buildDomainGroup(domain, bucket, alreadyDispatched, mentions, !dispatchedIds.isEmpty()));
        }

        if (!command.dryRun()) {
            for (FigmaWatchedFile file : files) {
                if (!erroredFileIds.contains(file.getId())) {
                    figmaWatchedFileStateUpdater.markIdle(file.getId());
                }
            }
        }

        if (command.advanceCursor() && !command.dryRun() && command.to() != null) {
            advanceCursor(command.to());
        }

        return new FigmaSummaryResult(
            command.from(),
            command.to(),
            totalComments,
            unmatchedCount,
            skippedAlreadyDispatched,
            domainGroups
        );
    }

    private List<FigmaWatchedFile> resolveTargetFiles(SummarizeFigmaCommentsCommand command) {
        if (command.singleFileId() != null) {
            return loadFigmaWatchedFilePort.findById(command.singleFileId())
                .map(List::of)
                .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_NOT_FOUND));
        }
        return loadFigmaWatchedFilePort.listEnabled(figmaSyncProperties.maxFilesPerRun());
    }

    private List<FigmaCommentInfo> filterByWindow(
        List<FigmaCommentInfo> comments,
        Instant windowFrom,
        Instant windowTo
    ) {
        return comments.stream()
            .filter(c -> c.createdAt() != null)
            .filter(c -> windowFrom == null || !c.createdAt().isBefore(windowFrom))
            .filter(c -> windowTo == null || !c.createdAt().isAfter(windowTo))
            .toList();
    }

    private List<String> sendDomainBatch(
        FigmaRoutingDomain domain,
        List<FigmaRoutingDomainMention> mentions,
        List<EnrichedComment> sendable,
        Instant windowFrom,
        Instant windowTo
    ) {
        List<String> mentionRenders = mentions.stream().map(FigmaRoutingDomainMention::render).toList();
        List<CommentEntry> entries = sendable.stream()
            .map(ec -> new CommentEntry(
                ec.comment.commentId(),
                ec.file.getDisplayName(),
                ec.pageName,
                ec.comment.authorName(),
                ec.comment.message(),
                buildCommentLink(ec.file, ec.comment),
                ec.comment.createdAt()
            ))
            .toList();

        try {
            Set<String> sent = sendDiscordMentionPort.send(new DiscordDomainBatchMessage(
                domain.getDiscordWebhookUrl(),
                domain.getDomainKey(),
                mentionRenders,
                windowFrom,
                windowTo,
                entries
            ));
            return new ArrayList<>(sent);
        } catch (FigmaDomainException e) {
            log.warn("Discord 도메인 batch 전송 실패. domainKey={}, count={}, error={}",
                domain.getDomainKey(), sendable.size(), e.getMessage());
            return List.of();
        }
    }

    private FigmaSummaryResult.DomainGroup buildDomainGroup(
        FigmaRoutingDomain domain,
        List<EnrichedComment> bucket,
        Set<String> alreadyDispatched,
        List<FigmaRoutingDomainMention> mentions,
        boolean sent
    ) {
        List<String> mentionRenders = mentions.stream()
            .map(FigmaRoutingDomainMention::render)
            .toList();
        List<FigmaSummaryResult.Comment> comments = bucket.stream()
            .map(ec -> new FigmaSummaryResult.Comment(
                ec.comment.commentId(),
                ec.comment.message(),
                ec.comment.authorName(),
                ec.file.getFileKey(),
                ec.file.getDisplayName(),
                ec.comment.nodeId(),
                ec.pageName,
                ec.classifiedDomainKey,
                ec.comment.createdAt(),
                alreadyDispatched.contains(ec.comment.commentId())
            ))
            .toList();
        return new FigmaSummaryResult.DomainGroup(
            domain.getDomainKey(),
            domain.getDiscordWebhookUrl(),
            domain.isFallback(),
            mentionRenders,
            sent,
            comments
        );
    }

    private void advanceCursor(Instant to) {
        FigmaSummaryCursor cursor = loadFigmaSummaryCursorPort.findCursor()
            .orElseGet(() -> FigmaSummaryCursor.bootstrap(to));
        cursor.advance(to);
        saveFigmaSummaryCursorPort.save(cursor);
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
            log.warn("페이지명 해석 실패. embed 의 pageName 필드는 비워집니다. fileKey={}", fileKey);
            return Map.of();
        }
    }

    private List<String> allCommentIds(Map<Long, List<EnrichedComment>> commentsByDomainId) {
        List<String> ids = new ArrayList<>();
        for (List<EnrichedComment> bucket : commentsByDomainId.values()) {
            for (EnrichedComment ec : bucket) {
                ids.add(ec.comment.commentId());
            }
        }
        return ids;
    }

    private String buildCommentLink(FigmaWatchedFile file, FigmaCommentInfo comment) {
        return String.format(
            "https://www.figma.com/file/%s?node-id=%s#%s",
            file.getFileKey(),
            comment.nodeId() == null ? "" : comment.nodeId(),
            comment.commentId()
        );
    }

    private record EnrichedComment(
        FigmaWatchedFile file,
        FigmaCommentInfo comment,
        String pageName,
        String classifiedDomainKey
    ) {
    }
}
