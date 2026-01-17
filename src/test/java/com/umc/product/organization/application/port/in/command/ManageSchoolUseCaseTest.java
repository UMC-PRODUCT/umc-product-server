package com.umc.product.organization.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ManageSchoolUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageSchoolUseCase manageSchoolUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private ManageChapterPort manageChapterPort;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private LoadSchoolPort loadSchoolPort;

    @Test
    void 지부_없이_학교를_등록한다() {
        // given
        CreateSchoolCommand command = new CreateSchoolCommand("동국대학교", null, "비고");

        // when
        Long schoolId = manageSchoolUseCase.register(command);

        // then
        School savedSchool = loadSchoolPort.findSchoolDetailById(schoolId);
        assertThat(savedSchool.getName()).isEqualTo("동국대학교");
        assertThat(savedSchool.getRemark()).isEqualTo("비고");
        assertThat(savedSchool.getChapterSchools()).isEmpty();
    }

    @Test
    void 지부와_함께_학교를_등록한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(8L));
        Chapter chapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Leo").build());

        CreateSchoolCommand command = new CreateSchoolCommand("동국대학교", chapter.getId(), "비고");

        // when
        Long schoolId = manageSchoolUseCase.register(command);

        // then
        School savedSchool = loadSchoolPort.findSchoolDetailById(schoolId);
        assertThat(savedSchool.getName()).isEqualTo("동국대학교");
        assertThat(savedSchool.getChapterSchools()).hasSize(1);
        assertThat(savedSchool.getChapterSchools().get(0).getChapter().getName()).isEqualTo("Leo");
    }

    @Test
    void 학교_이름과_비고를_수정한다() {
        // given
        School school = manageSchoolPort.save(School.create("동국대학교", "비고"));

        UpdateSchoolCommand command = new UpdateSchoolCommand("연세대학교", null, "수정된 비고");

        // when
        manageSchoolUseCase.updateSchool(school.getId(), command);

        // then
        School updatedSchool = loadSchoolPort.findById(school.getId());
        assertThat(updatedSchool.getName()).isEqualTo("연세대학교");
        assertThat(updatedSchool.getRemark()).isEqualTo("수정된 비고");
    }

    @Test
    void 학교의_지부를_수정한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(8L));
        Chapter scorpioChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio").build());
        Chapter leoChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Leo").build());

        School school = manageSchoolPort.save(School.create("동국대학교", "비고"));

        UpdateSchoolCommand command = new UpdateSchoolCommand("동국대학교", leoChapter.getId(), "비고");

        // when
        manageSchoolUseCase.updateSchool(school.getId(), command);

        // then
        School updatedSchool = loadSchoolPort.findSchoolDetailById(school.getId());
        assertThat(updatedSchool.getChapterSchools()).hasSize(1);
        assertThat(updatedSchool.getChapterSchools().get(0).getChapter().getName()).isEqualTo("Leo");
    }

    @Test
    void 존재하지_않는_학교를_수정하면_예외가_발생한다() {
        // given
        UpdateSchoolCommand command = new UpdateSchoolCommand("연세대학교", null, "비고");

        // when & then
        assertThatThrownBy(() -> manageSchoolUseCase.updateSchool(999L, command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 학교를_삭제한다() {
        // given
        School school1 = manageSchoolPort.save(School.create("동국대학교", "비고1"));
        School school2 = manageSchoolPort.save(School.create("연세대학교", "비고2"));

        // when
        manageSchoolUseCase.deleteSchools(List.of(school1.getId()));

        // then
        assertThatThrownBy(() -> loadSchoolPort.findById(school1.getId()))
                .isInstanceOf(BusinessException.class);

        School remainingSchool = loadSchoolPort.findById(school2.getId());
        assertThat(remainingSchool.getName()).isEqualTo("연세대학교");
    }

    @Test
    void 여러_학교를_한번에_삭제한다() {
        // given
        School school1 = manageSchoolPort.save(School.create("동국대학교", "비고1"));
        School school2 = manageSchoolPort.save(School.create("중앙대학교", "비고2"));
        School school3 = manageSchoolPort.save(School.create("한성대학교", "비고3"));

        // when
        manageSchoolUseCase.deleteSchools(List.of(school1.getId(), school2.getId()));

        // then
        assertThatThrownBy(() -> loadSchoolPort.findById(school1.getId()))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> loadSchoolPort.findById(school2.getId()))
                .isInstanceOf(BusinessException.class);

        School remainingSchool = loadSchoolPort.findById(school3.getId());
        assertThat(remainingSchool.getName()).isEqualTo("한성대학교");
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
