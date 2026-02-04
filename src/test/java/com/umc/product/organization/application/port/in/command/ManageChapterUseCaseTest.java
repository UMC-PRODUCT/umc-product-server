package com.umc.product.organization.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.application.port.in.command.dto.CreateChapterCommand;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ManageChapterUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageChapterUseCase manageChapterUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private LoadChapterPort loadChapterPort;

    @Autowired
    private LoadSchoolPort loadSchoolPort;

    @Test
    void 지부를_생성한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(9L));
        CreateChapterCommand command = new CreateChapterCommand(gisu.getId(), "Scorpio", List.of());

        // when
        Long chapterId = manageChapterUseCase.create(command);

        // then
        Chapter chapter = loadChapterPort.findById(chapterId);
        assertThat(chapter.getName()).isEqualTo("Scorpio");
        assertThat(chapter.getGisu().getId()).isEqualTo(gisu.getId());
    }

    @Test
    void 학교와_함께_지부를_생성한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(9L));
        School school1 = manageSchoolPort.save(School.create("한성대", null));
        School school2 = manageSchoolPort.save(School.create("동국대", null));

        CreateChapterCommand command = new CreateChapterCommand(
                gisu.getId(),
                "Scorpio",
                List.of(school1.getId(), school2.getId())
        );

        // when
        Long chapterId = manageChapterUseCase.create(command);

        // then
        Chapter chapter = loadChapterPort.findById(chapterId);
        assertThat(chapter.getName()).isEqualTo("Scorpio");

        School savedSchool1 = loadSchoolPort.findSchoolDetailById(school1.getId());
        School savedSchool2 = loadSchoolPort.findSchoolDetailById(school2.getId());

        assertThat(savedSchool1.getChapterSchools()).hasSize(1);
        assertThat(savedSchool1.getChapterSchools().get(0).getChapter().getId()).isEqualTo(chapterId);

        assertThat(savedSchool2.getChapterSchools()).hasSize(1);
        assertThat(savedSchool2.getChapterSchools().get(0).getChapter().getId()).isEqualTo(chapterId);
    }

    @Test
    void 존재하지_않는_학교로_지부를_생성하면_예외가_발생한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(9L));
        CreateChapterCommand command = new CreateChapterCommand(
                gisu.getId(),
                "서울",
                List.of(999L)
        );

        // when & then
        assertThatThrownBy(() -> manageChapterUseCase.create(command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 존재하지_않는_기수로_지부를_생성하면_예외가_발생한다() {
        // given
        CreateChapterCommand command = new CreateChapterCommand(999L, "Scorpio", List.of());

        // when & then
        assertThatThrownBy(() -> manageChapterUseCase.create(command))
                .isInstanceOf(BusinessException.class);
    }

    private Gisu createGisu(Long generation) {
        return Gisu.create(
                generation,
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-08-31T23:59:59Z"),
                true
        );
    }
}
