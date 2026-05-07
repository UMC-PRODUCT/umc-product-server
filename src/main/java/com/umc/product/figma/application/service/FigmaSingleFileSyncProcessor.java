package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.out.FetchFigmaCommentPort;
import com.umc.product.figma.application.port.out.FetchFigmaFileMetadataPort;
import com.umc.product.figma.application.port.out.LoadFigmaRoutingDomainPort;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SaveFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SendDiscordMentionPort;
import com.umc.product.figma.application.port.out.dto.DiscordMentionMessage;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.time.Instant;
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
 *
 * 단계:
 * 1. 신규 댓글 필터
 * 2. (display 용) 페이지명 best-effort 해석 — 라우팅에는 사용되지 않음
 * 3. LLM 분류기 호출 → domain_key
 * 4. domain_key 로 FigmaRoutingDomain lookup, 없으면 fallback 도메인
 * 5. 도메인의 mentions 모아 Discord 발송
 * 6. last_synced_comment_id 갱신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaSingleFileSyncProcessor {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final SaveFigmaWatchedFilePort saveFigmaWatchedFilePort;
    private final LoadFigmaRoutingDomainPort loadFigmaRoutingDomainPort;
    private final FetchFigmaCommentPort fetchFigmaCommentPort;
    private final FetchFigmaFileMetadataPort fetchFigmaFileMetadataPort;
    private final SendDiscordMentionPort sendDiscordMentionPort;
    private final FigmaCommentDomainClassifier figmaCommentDomainClassifier;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long watchedFileId, String accessToken) {
        FigmaWatchedFile watchedFile = loadFigmaWatchedFilePort.findById(watchedFileId).orElse(null);
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
            List<String> candidateKeys = domains.stream()
                .map(FigmaRoutingDomain::getDomainKey)
                .toList();

            for (FigmaCommentInfo comment : newComments) {
                String classifiedKey = figmaCommentDomainClassifier.classify(comment, candidateKeys);
                FigmaRoutingDomain matched = classifiedKey != null ? domainByKey.get(classifiedKey) : null;
                FigmaRoutingDomain applied = matched != null ? matched : fallback.orElse(null);
                if (applied == null) {
                    log.warn("매칭된 라우팅 도메인이 없고 fallback 도 없습니다. fileKey={}, commentId={}",
                        watchedFile.getFileKey(), comment.commentId());
                    continue;
                }
                String pageName = comment.nodeId() != null ? nodeIdToPageName.get(comment.nodeId()) : null;
                trySendMention(watchedFile, comment, applied, pageName);
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
            log.warn("페이지명 해석 실패. embed 의 pageName 필드는 비워집니다. fileKey={}", fileKey);
            return Map.of();
        }
    }

    private void trySendMention(
        FigmaWatchedFile watchedFile,
        FigmaCommentInfo comment,
        FigmaRoutingDomain domain,
        String pageName
    ) {
        try {
            List<FigmaRoutingDomainMention> mentions = loadFigmaRoutingDomainPort.listMentionsByDomainId(domain.getId());
            List<String> mentionRenders = mentions.stream().map(FigmaRoutingDomainMention::render).toList();
            String link = buildCommentLink(watchedFile, comment);

            sendDiscordMentionPort.send(new DiscordMentionMessage(
                domain.getDiscordWebhookUrl(),
                mentionRenders,
                watchedFile.getDisplayName(),
                domain.getDomainKey(),
                pageName,
                comment.authorName(),
                comment.message(),
                link,
                comment.createdAt()
            ));
        } catch (FigmaDomainException e) {
            log.warn("Discord 멘션 전송 실패. 다음 폴링에서 재발송하지 않습니다. fileKey={}, commentId={}",
                watchedFile.getFileKey(), comment.commentId());
        }
    }

    private String buildCommentLink(FigmaWatchedFile watchedFile, FigmaCommentInfo comment) {
        return String.format(
            "https://www.figma.com/file/%s?node-id=%s#%s",
            watchedFile.getFileKey(),
            comment.nodeId() == null ? "" : comment.nodeId(),
            comment.commentId()
        );
    }
}
