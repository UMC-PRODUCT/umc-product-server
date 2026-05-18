package com.umc.product.figma.application.port.out;

import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import java.util.List;

public interface FetchFigmaCommentPort {

    /**
     * 파일의 모든 댓글을 createdAt 오름차순으로 반환한다. 호출자가 시간창(from, to) 으로 필터링한다 (ADR-004 §Decision 1).
     */
    List<FigmaCommentInfo> listComments(String fileKey, String accessToken);
}
