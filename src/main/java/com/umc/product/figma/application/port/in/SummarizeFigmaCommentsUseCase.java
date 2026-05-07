package com.umc.product.figma.application.port.in;

import com.umc.product.figma.application.port.in.dto.FigmaSummaryResult;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;

/**
 * Figma 댓글 동기화의 단일 진입점 (ADR-004 §Decision 1).
 * <p>
 * 활성 watched file 들에서 시간창 [from, to] 안의 댓글을 모아 LLM 분류기로 도메인을 판별한 뒤, 도메인별로 묶어 Discord 로 발송한다. 세 진입점 (스케줄러 sync / admin
 * digest / admin preview) 은 같은 본체를 호출하며, 차이는 {@link SummarizeFigmaCommentsCommand} 의 dryRun / force / advanceCursor 플래그
 * 조합으로 표현된다.
 */
public interface SummarizeFigmaCommentsUseCase {

    FigmaSummaryResult summarize(SummarizeFigmaCommentsCommand command);
}
