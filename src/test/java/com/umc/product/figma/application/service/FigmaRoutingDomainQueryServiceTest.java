package com.umc.product.figma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.umc.product.figma.application.port.in.dto.FigmaRoutingDomainMentionInfo;
import com.umc.product.figma.application.port.in.dto.FigmaRoutingDomainSummaryInfo;
import com.umc.product.figma.application.port.out.LoadFigmaRoutingDomainPort;
import com.umc.product.figma.domain.FigmaRoutingDomain;
import com.umc.product.figma.domain.FigmaRoutingDomainMention;
import com.umc.product.figma.domain.enums.DiscordMentionType;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FigmaRoutingDomainQueryService")
@ExtendWith(MockitoExtension.class)
class FigmaRoutingDomainQueryServiceTest {

    @Mock
    private LoadFigmaRoutingDomainPort loadFigmaRoutingDomainPort;

    @InjectMocks
    private FigmaRoutingDomainQueryService figmaRoutingDomainQueryService;

    @Test
    @DisplayName("getDomainById_미존재면_ROUTING_DOMAIN_NOT_FOUND_예외")
    void 단건_미존재_예외() {
        when(loadFigmaRoutingDomainPort.findDomainById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> figmaRoutingDomainQueryService.getDomainById(99L))
            .isInstanceOf(FigmaDomainException.class)
            .extracting("baseCode")
            .isEqualTo(FigmaErrorCode.ROUTING_DOMAIN_NOT_FOUND);
    }

    @Test
    @DisplayName("getDomainById_단건_조회_시_mentions_까지_채워서_반환한다")
    void 단건_조회_시_mentions_포함() {
        FigmaRoutingDomain domain = domain(10L, "auth", false);
        FigmaRoutingDomainMention mention = mention(101L, 10L, "999", DiscordMentionType.ROLE, "백엔드 파트장");
        when(loadFigmaRoutingDomainPort.findDomainById(10L)).thenReturn(Optional.of(domain));
        when(loadFigmaRoutingDomainPort.listMentionsByDomainId(10L)).thenReturn(List.of(mention));

        FigmaRoutingDomainSummaryInfo info = figmaRoutingDomainQueryService.getDomainById(10L);

        assertThat(info.id()).isEqualTo(10L);
        assertThat(info.domainKey()).isEqualTo("auth");
        assertThat(info.mentionCount()).isEqualTo(1);
        assertThat(info.mentions())
            .singleElement()
            .extracting(FigmaRoutingDomainMentionInfo::id, FigmaRoutingDomainMentionInfo::displayLabel)
            .containsExactly(101L, "백엔드 파트장");
    }

    @Test
    @DisplayName("listDomains_는_mentions_본문_없이_mentionCount_만_채운다")
    void list_조회_mentionCount_만_채움() {
        FigmaRoutingDomain auth = domain(10L, "auth", false);
        FigmaRoutingDomain fallback = domain(20L, "fallback", true);
        when(loadFigmaRoutingDomainPort.listAllDomains()).thenReturn(List.of(auth, fallback));
        when(loadFigmaRoutingDomainPort.listMentionsByDomainId(10L))
            .thenReturn(List.of(mention(1L, 10L, "1", DiscordMentionType.ROLE, "a"),
                mention(2L, 10L, "2", DiscordMentionType.USER, "b")));
        when(loadFigmaRoutingDomainPort.listMentionsByDomainId(20L)).thenReturn(List.of());

        List<FigmaRoutingDomainSummaryInfo> result = figmaRoutingDomainQueryService.listDomains();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).mentions()).isNull();
        assertThat(result.get(0).mentionCount()).isEqualTo(2);
        assertThat(result.get(1).mentionCount()).isZero();
    }

    @Test
    @DisplayName("listMentionsByDomainId_는_도메인_미존재_시_예외_정상이면_mention_리스트_반환")
    void mention_조회_도메인_검증_후_반환() {
        when(loadFigmaRoutingDomainPort.findDomainById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> figmaRoutingDomainQueryService.listMentionsByDomainId(404L))
            .isInstanceOf(FigmaDomainException.class);

        FigmaRoutingDomain domain = domain(10L, "auth", false);
        FigmaRoutingDomainMention mention = mention(1L, 10L, "1", DiscordMentionType.ROLE, "a");
        lenient().when(loadFigmaRoutingDomainPort.findDomainById(10L)).thenReturn(Optional.of(domain));
        when(loadFigmaRoutingDomainPort.findDomainById(10L)).thenReturn(Optional.of(domain));
        when(loadFigmaRoutingDomainPort.listMentionsByDomainId(10L)).thenReturn(List.of(mention));

        List<FigmaRoutingDomainMentionInfo> mentions = figmaRoutingDomainQueryService.listMentionsByDomainId(10L);

        assertThat(mentions).singleElement()
            .extracting(FigmaRoutingDomainMentionInfo::mentionType)
            .isEqualTo(DiscordMentionType.ROLE);
    }

    private static FigmaRoutingDomain domain(Long id, String key, boolean fallback) {
        FigmaRoutingDomain domain = FigmaRoutingDomain.of(
            key,
            "desc",
            "https://discord.com/api/webhooks/123456789012345678/abcdefghijklmnopqr",
            fallback
        );
        setId(domain, "id", id);
        return domain;
    }

    private static FigmaRoutingDomainMention mention(
        Long id, Long domainId, String mentionId, DiscordMentionType type, String label
    ) {
        FigmaRoutingDomainMention mention = FigmaRoutingDomainMention.of(domainId, mentionId, type, label);
        setId(mention, "id", id);
        return mention;
    }

    private static void setId(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
