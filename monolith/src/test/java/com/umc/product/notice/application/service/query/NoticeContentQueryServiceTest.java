package com.umc.product.notice.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.form.application.port.in.query.GetVoteUseCase;
import com.umc.product.notice.application.port.out.LoadNoticeImagePort;
import com.umc.product.notice.application.port.out.LoadNoticeLinkPort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeImage;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;

@ExtendWith(MockitoExtension.class)
class NoticeContentQueryServiceTest {

    @Mock
    LoadNoticeVotePort loadNoticeVotePort;
    @Mock
    LoadNoticeImagePort loadNoticeImagePort;
    @Mock
    LoadNoticeLinkPort loadNoticeLinkPort;
    @Mock
    GetFileUseCase getFileUseCase;
    @Mock
    GetVoteUseCase getVoteUseCase;
    @InjectMocks
    NoticeContentQueryService sut;

    @Test
    @DisplayName("이미지 응답은 연결 ID를 유지하고 저장소 파일 ID를 별도로 제공한다")
    void 이미지_파일_ID_제공() {
        // Given
        String fileId = "550e8400-e29b-41d4-a716-446655440000";
        Notice notice = Notice.create("제목", "내용", 1L, false, false);
        NoticeImage image = NoticeImage.create(fileId, notice, 0);
        ReflectionTestUtils.setField(image, "id", 123L);
        when(loadNoticeImagePort.findImagesByNoticeId(42L)).thenReturn(List.of(image));
        when(getFileUseCase.getFileLinks(List.of(fileId))).thenReturn(Map.of(fileId, "https://example.com/image"));

        // When
        var result = sut.findImageByNoticeId(42L);
        var json = new ObjectMapper().valueToTree(result.getFirst());

        // Then
        assertThat(result).hasSize(1);
        assertThat(json.get("id").asLong()).isEqualTo(123L);
        assertThat(json.path("fileId").asText()).isEqualTo(fileId);
        assertThat(json.get("url").asText()).isEqualTo("https://example.com/image");
        assertThat(json.get("displayOrder").asInt()).isZero();
    }
}
