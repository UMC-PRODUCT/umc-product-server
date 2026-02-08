package com.umc.product.organization.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.application.port.in.command.dto.AssignSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UnassignSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.application.port.out.command.ManageChapterSchoolPort;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
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

    @Autowired
    private ManageChapterSchoolPort manageChapterSchoolPort;

    @Test
    void 학교를_등록한다() {
        // given
        CreateSchoolCommand command = new CreateSchoolCommand("한성대", "비고", null, null, null, null);

        // when
        Long schoolId = manageSchoolUseCase.register(command);

        // then
        School savedSchool = loadSchoolPort.findSchoolDetailById(schoolId);
        assertThat(savedSchool.getName()).isEqualTo("한성대");
        assertThat(savedSchool.getRemark()).isEqualTo("비고");
    }

    @Test
    void 외부링크와_함께_학교를_등록한다() {
        // given
        CreateSchoolCommand command = new CreateSchoolCommand(
                "한성대", "비고", null,
                "https://open.kakao.com/o/example",
                "https://instagram.com/example",
                "https://youtube.com/@example"
        );

        // when
        Long schoolId = manageSchoolUseCase.register(command);

        // then
        School savedSchool = loadSchoolPort.findSchoolDetailById(schoolId);
        assertThat(savedSchool.getName()).isEqualTo("한성대");
        assertThat(savedSchool.getKakaoLink()).isEqualTo("https://open.kakao.com/o/example");
        assertThat(savedSchool.getInstagramLink()).isEqualTo("https://instagram.com/example");
        assertThat(savedSchool.getYoutubeLink()).isEqualTo("https://youtube.com/@example");
    }

    @Test
    void 학교_이름과_비고를_수정한다() {
        // given
        School school = manageSchoolPort.save(School.create("한성대", "비고"));

        UpdateSchoolCommand command = new UpdateSchoolCommand("동국대", null, "수정된 비고", null);

        // when
        manageSchoolUseCase.updateSchool(school.getId(), command);

        // then
        School updatedSchool = loadSchoolPort.findById(school.getId());
        assertThat(updatedSchool.getName()).isEqualTo("동국대");
        assertThat(updatedSchool.getRemark()).isEqualTo("수정된 비고");
    }

    @Test
    void 학교의_지부를_수정한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(8L));
        Chapter scorpioChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio").build());
        Chapter leoChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Leo").build());

        School school = manageSchoolPort.save(School.create("한성대", "비고"));

        UpdateSchoolCommand command = new UpdateSchoolCommand("한성대", leoChapter.getId(), "비고", null);

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
        UpdateSchoolCommand command = new UpdateSchoolCommand("동국대", null, "비고", null);

        // when & then
        assertThatThrownBy(() -> manageSchoolUseCase.updateSchool(999L, command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 학교를_삭제한다() {
        // given
        School school1 = manageSchoolPort.save(School.create("한성대", "비고1"));
        School school2 = manageSchoolPort.save(School.create("동국대", "비고2"));

        // when
        manageSchoolUseCase.deleteSchools(List.of(school1.getId()));

        // then
        assertThatThrownBy(() -> loadSchoolPort.findById(school1.getId()))
                .isInstanceOf(BusinessException.class);

        School remainingSchool = loadSchoolPort.findById(school2.getId());
        assertThat(remainingSchool.getName()).isEqualTo("동국대");
    }

    @Test
    void 여러_학교를_한번에_삭제한다() {
        // given
        School school1 = manageSchoolPort.save(School.create("한성대", "비고1"));
        School school2 = manageSchoolPort.save(School.create("동국대", "비고2"));
        School school3 = manageSchoolPort.save(School.create("중앙대", "비고3"));

        // when
        manageSchoolUseCase.deleteSchools(List.of(school1.getId(), school2.getId()));

        // then
        assertThatThrownBy(() -> loadSchoolPort.findById(school1.getId()))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> loadSchoolPort.findById(school2.getId()))
                .isInstanceOf(BusinessException.class);

        School remainingSchool = loadSchoolPort.findById(school3.getId());
        assertThat(remainingSchool.getName()).isEqualTo("중앙대");
    }

    @Test
    void 학교를_지부에_배정한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(9L));
        Chapter chapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio").build());
        School school = manageSchoolPort.save(School.create("한성대", null));

        AssignSchoolCommand command = new AssignSchoolCommand(school.getId(), chapter.getId());

        // when
        manageSchoolUseCase.assignToChapter(command);

        // then
        School updatedSchool = loadSchoolPort.findSchoolDetailById(school.getId());
        assertThat(updatedSchool.getChapterSchools()).hasSize(1);
        assertThat(updatedSchool.getChapterSchools().get(0).getChapter().getName()).isEqualTo("Scorpio");
    }

    @Test
    void 이미_배정된_학교를_다른_지부로_이동한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(9L));
        Chapter scorpioChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio").build());
        Chapter leoChapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Leo").build());

        School school = manageSchoolPort.save(School.create("한성대", null));
        manageChapterSchoolPort.save(ChapterSchool.create(scorpioChapter, school));

        AssignSchoolCommand command = new AssignSchoolCommand(school.getId(), leoChapter.getId());

        // when
        manageSchoolUseCase.assignToChapter(command);

        // then
        School updatedSchool = loadSchoolPort.findSchoolDetailById(school.getId());
        assertThat(updatedSchool.getChapterSchools()).hasSize(1);
        assertThat(updatedSchool.getChapterSchools().get(0).getChapter().getName()).isEqualTo("Leo");
    }

    @Test
    void 다른_기수_배정은_유지하면서_특정_기수_지부로_배정한다() {
        // given
        Gisu gisu9 = manageGisuPort.save(createGisu(9L));
        Gisu gisu10 = manageGisuPort.save(createGisu(10L));

        Chapter chapter9 = manageChapterPort.save(Chapter.builder().gisu(gisu9).name("Scorpio").build());
        Chapter chapter10 = manageChapterPort.save(Chapter.builder().gisu(gisu10).name("Leo").build());

        School school = manageSchoolPort.save(School.create("한성대", null));
        manageChapterSchoolPort.save(ChapterSchool.create(chapter10, school));

        AssignSchoolCommand command = new AssignSchoolCommand(school.getId(), chapter9.getId());

        // when
        manageSchoolUseCase.assignToChapter(command);

        // then
        School updatedSchool = loadSchoolPort.findSchoolDetailById(school.getId());
        assertThat(updatedSchool.getChapterSchools()).hasSize(2);
        assertThat(updatedSchool.getChapterSchools())
                .extracting(cs -> cs.getChapter().getName())
                .containsExactlyInAnyOrder("Scorpio", "Leo");
    }

    @Test
    void 학교의_지부_배정을_해제한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(9L));
        Chapter chapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio").build());

        School school = manageSchoolPort.save(School.create("한성대", null));
        manageChapterSchoolPort.save(ChapterSchool.create(chapter, school));

        UnassignSchoolCommand command = new UnassignSchoolCommand(school.getId(), gisu.getId());

        // when
        manageSchoolUseCase.unassignFromChapter(command);

        // then
        School updatedSchool = loadSchoolPort.findSchoolDetailById(school.getId());
        assertThat(updatedSchool.getChapterSchools()).isEmpty();
    }

    @Test
    void 특정_기수의_배정만_해제하고_다른_기수는_유지한다() {
        // given
        Gisu gisu9 = manageGisuPort.save(createGisu(9L));
        Gisu gisu10 = manageGisuPort.save(createGisu(10L));

        Chapter chapter9 = manageChapterPort.save(Chapter.builder().gisu(gisu9).name("Scorpio").build());
        Chapter chapter10 = manageChapterPort.save(Chapter.builder().gisu(gisu10).name("Leo").build());

        School school = manageSchoolPort.save(School.create("한성대", null));
        manageChapterSchoolPort.save(ChapterSchool.create(chapter9, school));
        manageChapterSchoolPort.save(ChapterSchool.create(chapter10, school));

        UnassignSchoolCommand command = new UnassignSchoolCommand(school.getId(), gisu9.getId());

        // when
        manageSchoolUseCase.unassignFromChapter(command);

        // then
        School updatedSchool = loadSchoolPort.findSchoolDetailById(school.getId());
        assertThat(updatedSchool.getChapterSchools()).hasSize(1);
        assertThat(updatedSchool.getChapterSchools().get(0).getChapter().getName()).isEqualTo("Leo");
    }

    @Test
    void 존재하지_않는_학교를_배정하면_예외가_발생한다() {
        // given
        Gisu gisu = manageGisuPort.save(createGisu(9L));
        Chapter chapter = manageChapterPort.save(Chapter.builder().gisu(gisu).name("Scorpio").build());

        AssignSchoolCommand command = new AssignSchoolCommand(999L, chapter.getId());

        // when & then
        assertThatThrownBy(() -> manageSchoolUseCase.assignToChapter(command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 존재하지_않는_지부에_배정하면_예외가_발생한다() {
        // given
        School school = manageSchoolPort.save(School.create("한성대", null));

        AssignSchoolCommand command = new AssignSchoolCommand(school.getId(), 999L);

        // when & then
        assertThatThrownBy(() -> manageSchoolUseCase.assignToChapter(command))
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
