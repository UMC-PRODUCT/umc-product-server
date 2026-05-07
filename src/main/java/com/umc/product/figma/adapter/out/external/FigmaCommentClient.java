package com.umc.product.figma.adapter.out.external;

import com.umc.product.figma.application.port.out.FetchFigmaCommentPort;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaCommentClient implements FetchFigmaCommentPort {

    private static final String COMMENTS_URI_TEMPLATE = "https://api.figma.com/v1/files/%s/comments";

    private final RestClient restClient;

    @Override
    public List<FigmaCommentInfo> listComments(String fileKey, String accessToken) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                .uri(String.format(COMMENTS_URI_TEMPLATE, fileKey))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

            return parseComments(body);
        } catch (RestClientResponseException e) {
            log.warn("Figma 댓글 조회 실패: fileKey={}, status={}, body={}",
                fileKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FigmaDomainException(FigmaErrorCode.COMMENT_FETCH_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private List<FigmaCommentInfo> parseComments(Map<String, Object> body) {
        if (body == null) {
            return List.of();
        }
        Object rawComments = body.get("comments");
        if (!(rawComments instanceof List<?> list)) {
            return List.of();
        }

        List<FigmaCommentInfo> result = new ArrayList<>();
        for (Object raw : list) {
            if (!(raw instanceof Map<?, ?> entry)) {
                continue;
            }
            Map<String, Object> c = (Map<String, Object>) entry;
            String id = String.valueOf(c.get("id"));
            String message = (String) c.getOrDefault("message", "");
            String author = extractAuthor(c);
            String nodeId = extractNodeId(c);
            Instant createdAt = parseInstant((String) c.get("created_at"));

            result.add(new FigmaCommentInfo(id, message, author, nodeId, createdAt));
        }

        result.sort(Comparator.comparing(FigmaCommentInfo::createdAt));
        return result;
    }

    @SuppressWarnings("unchecked")
    private String extractAuthor(Map<String, Object> commentRaw) {
        Object user = commentRaw.get("user");
        if (user instanceof Map<?, ?> userMap) {
            Object handle = ((Map<String, Object>) userMap).get("handle");
            if (handle != null) {
                return String.valueOf(handle);
            }
        }
        return "unknown";
    }

    @SuppressWarnings("unchecked")
    private String extractNodeId(Map<String, Object> commentRaw) {
        Object meta = commentRaw.get("client_meta");
        if (meta instanceof Map<?, ?> metaMap) {
            Object nodeId = ((Map<String, Object>) metaMap).get("node_id");
            if (nodeId != null) {
                return String.valueOf(nodeId);
            }
        }
        return null;
    }

    private Instant parseInstant(String raw) {
        if (raw == null || raw.isBlank()) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(raw);
        } catch (DateTimeParseException e) {
            return Instant.EPOCH;
        }
    }
}
