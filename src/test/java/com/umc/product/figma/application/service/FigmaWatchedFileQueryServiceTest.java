package com.umc.product.figma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umc.product.figma.application.port.in.dto.FigmaWatchedFileInfo;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.domain.FigmaWatchedFile;
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

@DisplayName("FigmaWatchedFileQueryService")
@ExtendWith(MockitoExtension.class)
class FigmaWatchedFileQueryServiceTest {

    @Mock
    private LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;

    @InjectMocks
    private FigmaWatchedFileQueryService figmaWatchedFileQueryService;

    @Test
    @DisplayName("getById_미존재면_WATCHED_FILE_NOT_FOUND_예외")
    void 단건_미존재_예외() {
        when(loadFigmaWatchedFilePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> figmaWatchedFileQueryService.getById(99L))
            .isInstanceOf(FigmaDomainException.class)
            .extracting("baseCode")
            .isEqualTo(FigmaErrorCode.WATCHED_FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("getById_정상이면_엔티티의_sync_상태_필드까지_그대로_매핑된다")
    void 단건_조회_sync_상태_매핑() {
        FigmaWatchedFile file = watchedFile(7L, "abcdef", "디자인 시스템");
        when(loadFigmaWatchedFilePort.findById(7L)).thenReturn(Optional.of(file));

        FigmaWatchedFileInfo info = figmaWatchedFileQueryService.getById(7L);

        assertThat(info.id()).isEqualTo(7L);
        assertThat(info.fileKey()).isEqualTo("abcdef");
        assertThat(info.displayName()).isEqualTo("디자인 시스템");
        assertThat(info.enabled()).isTrue();
    }

    @Test
    @DisplayName("listAll_은_enabledFilter_를_그대로_outbound_port_에_위임한다")
    void 필터_위임() {
        FigmaWatchedFile a = watchedFile(1L, "fa", "A");
        FigmaWatchedFile b = watchedFile(2L, "fb", "B");
        when(loadFigmaWatchedFilePort.listAll(null)).thenReturn(List.of(a, b));
        when(loadFigmaWatchedFilePort.listAll(true)).thenReturn(List.of(a));
        when(loadFigmaWatchedFilePort.listAll(false)).thenReturn(List.of());

        assertThat(figmaWatchedFileQueryService.listAll(null)).hasSize(2);
        assertThat(figmaWatchedFileQueryService.listAll(true)).singleElement()
            .extracting(FigmaWatchedFileInfo::id).isEqualTo(1L);
        assertThat(figmaWatchedFileQueryService.listAll(false)).isEmpty();

        verify(loadFigmaWatchedFilePort).listAll(null);
        verify(loadFigmaWatchedFilePort).listAll(true);
        verify(loadFigmaWatchedFilePort).listAll(false);
    }

    private static FigmaWatchedFile watchedFile(Long id, String fileKey, String displayName) {
        FigmaWatchedFile file = FigmaWatchedFile.of(fileKey, displayName);
        try {
            Field field = FigmaWatchedFile.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(file, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return file;
    }
}
