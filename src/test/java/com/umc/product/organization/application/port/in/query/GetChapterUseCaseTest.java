package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetChapterUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetChapterUseCase getChapterUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private ManageChapterPort manageChapterPort;

    @Test
    void 전체_지부_목록을_조회한다() {
        // given
        Gisu gisu = createGisu(8L);
        manageGisuPort.save(gisu);

        manageChapterPort.save(Chapter.builder().gisu(gisu).name("서울").build());
        manageChapterPort.save(Chapter.builder().gisu(gisu).name("경기").build());
        manageChapterPort.save(Chapter.builder().gisu(gisu).name("인천").build());

        // when
        List<ChapterInfo> result = getChapterUseCase.getAllChapters();

        // then
        assertThat(result).hasSize(3).extracting(ChapterInfo::name)
                .containsExactlyInAnyOrder("서울", "경기", "인천");
    }


    private Gisu createGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
                .build();
    }
}
