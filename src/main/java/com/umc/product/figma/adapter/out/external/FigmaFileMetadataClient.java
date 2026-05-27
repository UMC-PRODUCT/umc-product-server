package com.umc.product.figma.adapter.out.external;

import com.umc.product.figma.application.port.out.FetchFigmaFileMetadataPort;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaFileMetadataClient implements FetchFigmaFileMetadataPort {

    private static final String FILE_NODES_URI_TEMPLATE =
        "https://api.figma.com/v1/files/%s/nodes?ids=%s";

    private final RestClient restClient;

    @Override
    public Map<String, String> resolvePageNames(String fileKey, String accessToken, Set<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return Map.of();
        }

        String idsParam = String.join(",", nodeIds);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                .uri(String.format(FILE_NODES_URI_TEMPLATE, fileKey, idsParam))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

            return parsePageNames(body, nodeIds);
        } catch (RestClientResponseException e) {
            log.warn("Figma 파일 메타데이터 조회 실패: fileKey={}, status={}, body={}",
                fileKey, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FigmaDomainException(FigmaErrorCode.FILE_METADATA_FETCH_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parsePageNames(Map<String, Object> body, Set<String> nodeIds) {
        if (body == null) {
            return Map.of();
        }

        Map<String, Object> nodes = (Map<String, Object>) body.get("nodes");
        if (nodes == null || nodes.isEmpty()) {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();
        for (String nodeId : nodeIds) {
            Object entry = nodes.get(nodeId);
            if (!(entry instanceof Map<?, ?> entryMap)) {
                continue;
            }

            String pageName = findEnclosingPageName((Map<String, Object>) entryMap);
            if (pageName != null) {
                result.put(nodeId, pageName);
            }
        }
        return result;
    }

    /**
     * /v1/files/:file_key/nodes 응답은 요청된 노드 별로 document subtree를 반환한다. 응답이 페이지 노드를 직접 주지는 않으므로, ancestors 필드가 제공되면 그 중
     * CANVAS 타입을, 그렇지 않으면 entry.document.name(노드 자신의 이름)을 fallback으로 사용한다.
     */
    @SuppressWarnings("unchecked")
    private String findEnclosingPageName(Map<String, Object> entry) {
        Object ancestors = entry.get("ancestors");
        if (ancestors instanceof List<?> list) {
            for (int i = list.size() - 1; i >= 0; i--) {
                Object ancestor = list.get(i);
                if (ancestor instanceof Map<?, ?> ancestorMap) {
                    Map<String, Object> a = (Map<String, Object>) ancestorMap;
                    if ("CANVAS".equals(a.get("type"))) {
                        Object name = a.get("name");
                        if (name != null) {
                            return String.valueOf(name);
                        }
                    }
                }
            }
        }
        Object document = entry.get("document");
        if (document instanceof Map<?, ?> docMap) {
            Object name = ((Map<String, Object>) docMap).get("name");
            if (name != null) {
                return String.valueOf(name);
            }
        }
        return null;
    }
}
