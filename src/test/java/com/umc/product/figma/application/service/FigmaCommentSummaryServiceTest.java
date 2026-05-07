package com.umc.product.figma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umc.product.figma.adapter.out.external.FigmaSyncProperties;
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
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaWatchedFile;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("FigmaCommentSummaryService")
@ExtendWith(MockitoExtension.class)
class FigmaCommentSummaryServiceTest {

    private static final String FILE_KEY = "FK1";
    private static final String DOMAIN_KEY = "auth";
    private static final String COMMENT_ID = "C1";

    @Mock private LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    @Mock private LoadFigmaRoutingDomainPort loadFigmaRoutingDomainPort;
    @Mock private FetchFigmaCommentPort fetchFigmaCommentPort;
    @Mock private FetchFigmaFileMetadataPort fetchFigmaFileMetadataPort;
    @Mock private SendDiscordMentionPort sendDiscordMentionPort;
    @Mock private LoadFigmaCommentDispatchPort loadFigmaCommentDispatchPort;
    @Mock private SaveFigmaCommentDispatchPort saveFigmaCommentDispatchPort;
    @Mock private LoadFigmaSummaryCursorPort loadFigmaSummaryCursorPort;
    @Mock private SaveFigmaSummaryCursorPort saveFigmaSummaryCursorPort;
    @Mock private FigmaIntegrationCommandService figmaIntegrationCommandService;
    @Mock private FigmaCommentDomainClassifier figmaCommentDomainClassifier;
    @Mock private FigmaWatchedFileStateUpdater figmaWatchedFileStateUpdater;

    private FigmaCommentSummaryService service;

    @BeforeEach
    void setUp() {
        FigmaSyncProperties syncProperties = new FigmaSyncProperties(true, Duration.ofMinutes(5), 50);
        service = new FigmaCommentSummaryService(
            loadFigmaWatchedFilePort,
            loadFigmaRoutingDomainPort,
            fetchFigmaCommentPort,
            fetchFigmaFileMetadataPort,
            sendDiscordMentionPort,
            loadFigmaCommentDispatchPort,
            saveFigmaCommentDispatchPort,
            loadFigmaSummaryCursorPort,
            saveFigmaSummaryCursorPort,
            figmaIntegrationCommandService,
            figmaCommentDomainClassifier,
            figmaWatchedFileStateUpdater,
            syncProperties
        );

        // 기본 fixtures
        FigmaWatchedFile file = givenWatchedFile();
        FigmaRoutingDomain domain = givenRoutingDomain();
        lenient().when(loadFigmaWatchedFilePort.listEnabled(anyInt())).thenReturn(List.of(file));
        lenient().when(figmaIntegrationCommandService.resolveActiveAccessToken()).thenReturn("AT");
        lenient().when(loadFigmaRoutingDomainPort.listAllDomains()).thenReturn(List.of(domain));
        lenient().when(loadFigmaRoutingDomainPort.listMentionsByDomainId(anyLong())).thenReturn(List.of());
        lenient().when(fetchFigmaFileMetadataPort.resolvePageNames(anyString(), anyString(), any())).thenReturn(Map.of());
    }

    @Test
    @DisplayName("sync 첫 호출에서는 dispatch 가 비어 있어 댓글 1건이 발송되고 dispatch 에 기록된다")
    void sync_첫_호출_발송_및_dispatch_기록() {
        FigmaCommentInfo c = comment(Instant.parse("2026-05-07T09:58:00Z"));
        when(fetchFigmaCommentPort.listComments(eq_(FILE_KEY), anyString())).thenReturn(List.of(c));
        when(figmaCommentDomainClassifier.classifyBatch(any(), any())).thenReturn(Map.of(COMMENT_ID, DOMAIN_KEY));
        when(loadFigmaCommentDispatchPort.findDispatchedCommentIds(anyCollection())).thenReturn(Set.of());

        FigmaSummaryResult result = service.summarize(SummarizeFigmaCommentsCommand.scheduledSync(
            Instant.parse("2026-05-07T09:55:00Z"),
            Instant.parse("2026-05-07T10:00:00Z")
        ));

        assertThat(result.totalComments()).isEqualTo(1);
        assertThat(result.skippedAlreadyDispatchedCount()).isZero();
        assertThat(result.domains()).hasSize(1);
        assertThat(result.domains().get(0).sent()).isTrue();
        verify(sendDiscordMentionPort, times(1)).send(any());
        verify(saveFigmaCommentDispatchPort, times(1)).recordDispatched(anyCollection(), anyLong(), any());
        verify(saveFigmaSummaryCursorPort, times(1)).save(any());
    }

    @Test
    @DisplayName("sync 두 번째 호출은 dispatch 행이 있어 발송이 건너뛰어지고 skippedAlreadyDispatched 가 증가한다")
    void sync_재호출_dispatch_dedup() {
        FigmaCommentInfo c = comment(Instant.parse("2026-05-07T09:58:00Z"));
        when(fetchFigmaCommentPort.listComments(eq_(FILE_KEY), anyString())).thenReturn(List.of(c));
        when(figmaCommentDomainClassifier.classifyBatch(any(), any())).thenReturn(Map.of(COMMENT_ID, DOMAIN_KEY));
        // 두 번째 호출 시점에 이미 dispatch 존재
        when(loadFigmaCommentDispatchPort.findDispatchedCommentIds(anyCollection())).thenReturn(Set.of(COMMENT_ID));

        FigmaSummaryResult result = service.summarize(SummarizeFigmaCommentsCommand.scheduledSync(
            Instant.parse("2026-05-07T09:55:00Z"),
            Instant.parse("2026-05-07T10:00:00Z")
        ));

        assertThat(result.totalComments()).isEqualTo(1);
        assertThat(result.skippedAlreadyDispatchedCount()).isEqualTo(1);
        assertThat(result.domains().get(0).sent()).isFalse();
        assertThat(result.domains().get(0).comments().get(0).alreadyDispatched()).isTrue();
        verify(sendDiscordMentionPort, never()).send(any());
        verify(saveFigmaCommentDispatchPort, never()).recordDispatched(anyCollection(), anyLong(), any());
    }

    @Test
    @DisplayName("digest force 모드에서는 dispatch 행이 있어도 재발송되고 skipped 는 0 으로 유지된다")
    void digest_force_dispatch_무시() {
        FigmaCommentInfo c = comment(Instant.parse("2026-05-07T09:58:00Z"));
        when(fetchFigmaCommentPort.listComments(eq_(FILE_KEY), anyString())).thenReturn(List.of(c));
        when(figmaCommentDomainClassifier.classifyBatch(any(), any())).thenReturn(Map.of(COMMENT_ID, DOMAIN_KEY));
        when(loadFigmaCommentDispatchPort.findDispatchedCommentIds(anyCollection())).thenReturn(Set.of(COMMENT_ID));

        FigmaSummaryResult result = service.summarize(SummarizeFigmaCommentsCommand.digest(
            Instant.parse("2026-05-07T09:55:00Z"),
            Instant.parse("2026-05-07T10:00:00Z")
        ));

        assertThat(result.skippedAlreadyDispatchedCount()).isZero();
        assertThat(result.domains().get(0).sent()).isTrue();
        // alreadyDispatched 플래그는 정보 차원에서 여전히 true 로 노출된다.
        assertThat(result.domains().get(0).comments().get(0).alreadyDispatched()).isTrue();
        verify(sendDiscordMentionPort, times(1)).send(any());
        // digest 는 dispatch 기록도 남기지만 cursor 는 advance 하지 않는다.
        verify(saveFigmaSummaryCursorPort, never()).save(any());
    }

    @Test
    @DisplayName("preview dryRun 은 발송/dispatch 기록/cursor advance 를 모두 수행하지 않는다")
    void preview_dryRun_부수효과_없음() {
        FigmaCommentInfo c = comment(Instant.parse("2026-05-07T09:58:00Z"));
        when(fetchFigmaCommentPort.listComments(eq_(FILE_KEY), anyString())).thenReturn(List.of(c));
        when(figmaCommentDomainClassifier.classifyBatch(any(), any())).thenReturn(Map.of(COMMENT_ID, DOMAIN_KEY));
        when(loadFigmaCommentDispatchPort.findDispatchedCommentIds(anyCollection())).thenReturn(Set.of(COMMENT_ID));

        FigmaSummaryResult result = service.summarize(SummarizeFigmaCommentsCommand.preview(
            Instant.parse("2026-05-07T09:55:00Z"),
            Instant.parse("2026-05-07T10:00:00Z")
        ));

        assertThat(result.totalComments()).isEqualTo(1);
        // dryRun 이라 sent 는 false 지만 alreadyDispatched 는 정확히 노출된다.
        assertThat(result.domains().get(0).sent()).isFalse();
        assertThat(result.domains().get(0).comments().get(0).alreadyDispatched()).isTrue();
        verify(sendDiscordMentionPort, never()).send(any());
        verify(saveFigmaCommentDispatchPort, never()).recordDispatched(anyCollection(), anyLong(), any());
        verify(saveFigmaSummaryCursorPort, never()).save(any());
        verify(figmaWatchedFileStateUpdater, never()).markIdle(anyLong());
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }

    /** {@code anyString()} 과 동작은 같지만, eq() 와 혼동되지 않게 따로 명명한 매처. */
    private static String eq_(String s) {
        return org.mockito.ArgumentMatchers.eq(s);
    }

    private FigmaCommentInfo comment(Instant createdAt) {
        return new FigmaCommentInfo(COMMENT_ID, "본문", "tester", "0:1", createdAt);
    }

    private FigmaWatchedFile givenWatchedFile() {
        FigmaWatchedFile file = FigmaWatchedFile.of(FILE_KEY, "test-file");
        ReflectionTestUtils.setField(file, "id", 100L);
        return file;
    }

    private FigmaRoutingDomain givenRoutingDomain() {
        FigmaRoutingDomain domain = FigmaRoutingDomain.of(DOMAIN_KEY, "인증", "https://discord.example/wh", false);
        ReflectionTestUtils.setField(domain, "id", 1L);
        return domain;
    }
}
