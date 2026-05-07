package com.umc.product.figma.adapter.in.web;

import com.umc.product.figma.application.port.in.DigestFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.DigestFigmaCommentsCommand;
import com.umc.product.figma.application.port.in.dto.FigmaDigestSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운영진이 sync 주기/상태와 무관하게 [from, to] 시간창의 댓글을 도메인별로 묶어 Discord 로 보내고 JSON 요약을 받아볼 수 있는 admin API.
 * <p>
 * 정기 sync 와 달리 last_synced_comment_id 를 갱신하지 않으므로, 동일 시간창을 여러 번 호출해도 Discord 에는 그때마다 다시 발송된다 (운영진이 의도한 catch-up /
 * 회고용).
 */
@RestController
@RequestMapping("/api/v1/admin/figma/digest")
@RequiredArgsConstructor
@Tag(name = "Figma Digest | 시간창 catch-up", description = "[from, to] 안의 댓글을 도메인별로 묶어 Discord 로 발송")
public class FigmaDigestController {

    private final DigestFigmaCommentsUseCase digestFigmaCommentsUseCase;

    @Operation(summary = "[FIGMA-015] 시간창 catch-up 발송")
    @PostMapping
    public FigmaDigestSummary digest(
        @RequestParam("from") Instant from,
        @RequestParam("to") Instant to
    ) {
        return digestFigmaCommentsUseCase.digest(new DigestFigmaCommentsCommand(from, to));
    }
}
