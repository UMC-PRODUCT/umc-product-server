package com.umc.product.figma.application.port.out;

import java.util.Map;

public interface FetchFigmaFileMetadataPort {

    /**
     * 주어진 nodeId 들에 대해 가장 가까운 페이지(CANVAS) 노드 이름을 해석한다. 결과는 nodeId → pageName. 노드가 없으면 해당 키는 결과에서 빠진다.
     */
    Map<String, String> resolvePageNames(String fileKey, String accessToken, java.util.Set<String> nodeIds);
}
