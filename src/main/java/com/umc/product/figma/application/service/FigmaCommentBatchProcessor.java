package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.out.FetchFigmaCommentPort;
import com.umc.product.figma.application.port.out.FetchFigmaFileMetadataPort;
import com.umc.product.figma.application.port.out.LoadFigmaRoutingDomainPort;
import com.umc.product.figma.application.port.out.SendDiscordMentionPort;
import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage;
import com.umc.product.figma.application.port.out.dto.DiscordDomainBatchMessage.CommentEntry;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
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
import org.springframework.stereotype.Component;

/**
 * 사이클 단위로 활성 파일들의 신규 댓글을 모아 분류 후 도메인별로 묶어 Discord 로 발송한다.
 * <p>
 * 두 가지 진입점:
 * <ul>
 *   <li>{@link #processSyncCycle(List, String)} — 정기 sync. 파일별 last_synced_comment_id 이후 댓글을
 *       대상으로 하고, 발송 후 last_synced_comment_id 를 갱신한다.</li>
 *   <li>{@link #processDigestWindow(List, String, Instant, Instant)} — 운영진의 catch-up. 시간창 [from, to]
 *       에 속한 댓글을 대상으로 하고, sync 상태는 변경하지 않는다.</li>
 * </ul>
 * <p>
 * 발송 실패는 도메인 묶음 단위로 격리된다. 한 도메인의 발송이 실패해도 다른 도메인 발송과
 * sync state 갱신은 계속 진행된다 (중복 발송 방지 우선).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaCommentBatchProcessor {

    private final LoadFigmaRoutingDomainPort loadFigmaRoutingDomainPort;
    private final FetchFigmaCommentPort fetchFigmaCommentPort;
    private final FetchFigmaFileMetadataPort fetchFigmaFileMetadataPort;
    private final SendDiscordMentionPort sendDiscordMentionPort;
    private final FigmaCommentDomainClassifier figmaCommentDomainClassifier;
    private final FigmaWatchedFileStateUpdater figmaWatchedFileStateUpdater;

    public BatchSummary processSyncCycle(List<FigmaWatchedFile> files, String accessToken) {
        return process(files, accessToken, /* windowFrom */ null, /* windowTo */ Instant.now(),
            /* mode */ Mode.SYNC);
    }

    public BatchSummary processDigestWindow(List<FigmaWatchedFile> files, String accessToken,
                                            Instant from, Instant to) {
        return process(files, accessToken, from, to, Mode.DIGEST);
    }

    private BatchSummary process(List<FigmaWatchedFile> files, String accessToken,
                                 Instant windowFrom, Instant windowTo, Mode mode) {
        if (files.isEmpty()) {
            return BatchSummary.empty();
        }

        List<FigmaRoutingDomain> domains = loadFigmaRoutingDomainPort.listAllDomains();
        if (domains.isEmpty()) {
            throw new FigmaDomainException(FigmaErrorCode.ROUTING_DOMAIN_NOT_REGISTERED);
        }
        Map<String, FigmaRoutingDomain> domainByKey = domains.stream()
            .collect(Collectors.toMap(FigmaRoutingDomain::getDomainKey, d -> d, (a, b) -> a));
        Optional<FigmaRoutingDomain> fallback = domains.stream()
            .filter(FigmaRoutingDomain::isFallback)
            .findFirst()
            .or(loadFigmaRoutingDomainPort::findFallbackDomain);
        List<String> candidateKeys = domains.stream().map(FigmaRoutingDomain::getDomainKey).toList();

        Map<Long, List<EnrichedComment>> commentsByDomainId = new LinkedHashMap<>();
        int totalComments = 0;
        int unmatchedCount = 0;

        for (FigmaWatchedFile file : files) {
            try {
                List<FigmaCommentInfo> comments = fetchFigmaCommentPort.listComments(file.getFileKey(), accessToken);
                List<FigmaCommentInfo> filtered = filter(comments, file, windowFrom, windowTo, mode);
                if (filtered.isEmpty()) {
                    if (mode == Mode.SYNC) {
                        figmaWatchedFileStateUpdater.markIdle(file.getId());
                    }
                    continue;
                }
                Map<String, String> nodeIdToPageName = resolvePageNames(file.getFileKey(), accessToken, filtered);
                Map<String, String> classifications = figmaCommentDomainClassifier.classifyBatch(filtered, candidateKeys);

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
                        .add(new EnrichedComment(file, c, pageName));
                    totalComments++;
                }
            } catch (FigmaDomainException e) {
                log.warn("Figma 댓글 수집 실패: fileKey={}, message={}", file.getFileKey(), e.getMessage());
                if (mode == Mode.SYNC) {
                    figmaWatchedFileStateUpdater.recordError(file.getId(), e.getMessage());
                }
            }
        }

        Map<Long, FigmaRoutingDomain> domainById = domains.stream()
            .collect(Collectors.toMap(FigmaRoutingDomain::getId, d -> d));
        List<DomainSendResult> sendResults = new ArrayList<>();
        for (Map.Entry<Long, List<EnrichedComment>> entry : commentsByDomainId.entrySet()) {
            FigmaRoutingDomain domain = domainById.get(entry.getKey());
            if (domain == null) {
                continue;
            }
            sendResults.add(sendDomainBatch(domain, entry.getValue(), windowFrom, windowTo));
        }

        if (mode == Mode.SYNC) {
            advanceLastSyncedPerFile(files, commentsByDomainId);
        }

        return new BatchSummary(totalComments, unmatchedCount, sendResults);
    }

    private List<FigmaCommentInfo> filter(
        List<FigmaCommentInfo> comments,
        FigmaWatchedFile file,
        Instant windowFrom,
        Instant windowTo,
        Mode mode
    ) {
        if (mode == Mode.SYNC) {
            return filterAfterLastSynced(comments, file.getLastSyncedCommentId());
        }
        return comments.stream()
            .filter(c -> c.createdAt() != null)
            .filter(c -> windowFrom == null || !c.createdAt().isBefore(windowFrom))
            .filter(c -> windowTo == null || !c.createdAt().isAfter(windowTo))
            .toList();
    }

    private List<FigmaCommentInfo> filterAfterLastSynced(List<FigmaCommentInfo> comments, String lastSyncedCommentId) {
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

    private DomainSendResult sendDomainBatch(
        FigmaRoutingDomain domain,
        List<EnrichedComment> comments,
        Instant windowFrom,
        Instant windowTo
    ) {
        List<FigmaRoutingDomainMention> mentions = loadFigmaRoutingDomainPort.listMentionsByDomainId(domain.getId());
        List<String> mentionRenders = mentions.stream().map(FigmaRoutingDomainMention::render).toList();
        List<CommentEntry> entries = comments.stream()
            .map(ec -> new CommentEntry(
                ec.file.getDisplayName(),
                ec.pageName,
                ec.comment.authorName(),
                ec.comment.message(),
                buildCommentLink(ec.file, ec.comment),
                ec.comment.createdAt()
            ))
            .toList();

        try {
            sendDiscordMentionPort.send(new DiscordDomainBatchMessage(
                domain.getDiscordWebhookUrl(),
                domain.getDomainKey(),
                mentionRenders,
                windowFrom,
                windowTo,
                entries
            ));
            return new DomainSendResult(domain.getDomainKey(), comments.size(), true);
        } catch (FigmaDomainException e) {
            log.warn("Discord 도메인 batch 전송 실패. 다음 사이클 재발송하지 않습니다. domainKey={}, count={}",
                domain.getDomainKey(), comments.size());
            return new DomainSendResult(domain.getDomainKey(), comments.size(), false);
        }
    }

    private void advanceLastSyncedPerFile(
        List<FigmaWatchedFile> files,
        Map<Long, List<EnrichedComment>> commentsByDomainId
    ) {
        Map<Long, FigmaCommentInfo> latestCommentByFile = new LinkedHashMap<>();
        for (List<EnrichedComment> bucket : commentsByDomainId.values()) {
            for (EnrichedComment ec : bucket) {
                latestCommentByFile.merge(
                    ec.file.getId(),
                    ec.comment,
                    (existing, candidate) -> existing.createdAt() == null
                        || (candidate.createdAt() != null && candidate.createdAt().isAfter(existing.createdAt()))
                        ? candidate : existing
                );
            }
        }
        for (Map.Entry<Long, FigmaCommentInfo> entry : latestCommentByFile.entrySet()) {
            figmaWatchedFileStateUpdater.advance(entry.getKey(), entry.getValue().commentId());
        }
        // 분류 후 도메인 매칭에 실패해 묶음에 들어가지 못한 파일도 idle 마킹으로 last_synced_at 만 갱신
        for (FigmaWatchedFile file : files) {
            if (!latestCommentByFile.containsKey(file.getId())) {
                figmaWatchedFileStateUpdater.markIdle(file.getId());
            }
        }
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

    private String buildCommentLink(FigmaWatchedFile file, FigmaCommentInfo comment) {
        return String.format(
            "https://www.figma.com/file/%s?node-id=%s#%s",
            file.getFileKey(),
            comment.nodeId() == null ? "" : comment.nodeId(),
            comment.commentId()
        );
    }

    private enum Mode {SYNC, DIGEST}

    private record EnrichedComment(FigmaWatchedFile file, FigmaCommentInfo comment, String pageName) {
    }

    public record DomainSendResult(String domainKey, int commentCount, boolean sent) {
    }

    public record BatchSummary(int totalComments, int unmatchedCount, List<DomainSendResult> sendResults) {
        public static BatchSummary empty() {
            return new BatchSummary(0, 0, List.of());
        }
    }
}
