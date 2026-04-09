package com.umc.product.organization.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.UnassignedSchoolInfo;
import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.application.port.out.command.ManageChapterSchoolPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.GisuFixture;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetSchoolUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetSchoolUseCase getSchoolUseCase;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private ManageChapterPort manageChapterPort;

    @Autowired
    private ManageChapterSchoolPort manageChapterSchoolPort;

    @Autowired
    private SaveFileMetadataPort saveFileMetadataPort;

    @Test
    void 배정_대기_중인_학교_목록을_조회한다() {
        // given
        Gisu gisu9 = gisuFixture.활성_기수(9L);
        Chapter scorpioChapter = manageChapterPort.save(Chapter.create(gisu9, "Scorpio"));

        School assignedSchool = manageSchoolPort.save(School.create("한성대", null));
        School unassignedSchool1 = manageSchoolPort.save(School.create("동국대", null));
        School unassignedSchool2 = manageSchoolPort.save(School.create("중앙대", null));

        manageChapterSchoolPort.save(ChapterSchool.create(scorpioChapter, assignedSchool));

        // when
        List<UnassignedSchoolInfo> result = getSchoolUseCase.getUnassignedSchools(gisu9.getId());

        // then
        assertThat(result).hasSize(2)
                .extracting(UnassignedSchoolInfo::schoolName)
                .containsExactlyInAnyOrder("동국대", "중앙대");
    }

    @Test
    void 다른_기수에_배정된_학교는_배정_대기로_조회된다() {
        // given
        Gisu gisu9 = gisuFixture.활성_기수(9L);
        Gisu gisu10 = gisuFixture.활성_기수(10L);

        Chapter chapter9 = manageChapterPort.save(Chapter.create(gisu9, "Scorpio"));
        Chapter chapter10 = manageChapterPort.save(Chapter.create(gisu10, "Leo"));

        School school1 = manageSchoolPort.save(School.create("한성대", null));
        School school2 = manageSchoolPort.save(School.create("동국대", null));

        manageChapterSchoolPort.save(ChapterSchool.create(chapter9, school1));
        manageChapterSchoolPort.save(ChapterSchool.create(chapter10, school2));

        // when
        List<UnassignedSchoolInfo> result = getSchoolUseCase.getUnassignedSchools(gisu9.getId());

        // then
        assertThat(result).hasSize(1)
                .extracting(UnassignedSchoolInfo::schoolName)
                .containsExactly("동국대");
    }

    @Test
    void 모든_학교가_배정되어_있으면_빈_목록을_반환한다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(9L);
        Chapter chapter = manageChapterPort.save(Chapter.create(gisu, "Scorpio"));

        School school = manageSchoolPort.save(School.create("한성대", null));
        manageChapterSchoolPort.save(ChapterSchool.create(chapter, school));

        // when
        List<UnassignedSchoolInfo> result = getSchoolUseCase.getUnassignedSchools(gisu.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 학교_상세를_조회한다_활성_기수_지부_정보를_포함한다() {
        // given
        Gisu gisu = gisuFixture.활성_기수(8L);
        Chapter chapter = manageChapterPort.save(Chapter.create(gisu, "Ain"));

        School school = School.create("중앙대", "비고");
        school.updateChapterSchool(chapter);
        manageSchoolPort.save(school);

        // when
        SchoolDetailInfo result = getSchoolUseCase.getSchoolDetail(school.getId());

        // then
        assertThat(result.schoolId()).isEqualTo(school.getId());
        assertThat(result.schoolName()).isEqualTo("중앙대");
        assertThat(result.remark()).isEqualTo("비고");
        assertThat(result.chapterId()).isEqualTo(chapter.getId());
        assertThat(result.chapterName()).isEqualTo("Ain");
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    void 비활성_기수_지부는_상세_조회에서_null로_반환된다() {
        // given
        Gisu inactiveGisu = gisuFixture.비활성_기수(7L);
        Chapter inactiveChapter = manageChapterPort.save(Chapter.create(inactiveGisu, "Scorpio"));

        School school = School.create("동국대", "비고");
        school.updateChapterSchool(inactiveChapter);
        manageSchoolPort.save(school);

        // when
        SchoolDetailInfo result = getSchoolUseCase.getSchoolDetail(school.getId());

        // then
        assertThat(result.schoolId()).isEqualTo(school.getId());
        assertThat(result.chapterId()).isNull();
        assertThat(result.chapterName()).isNull();
    }

    @Test
    void 전체_학교_이름_목록을_조회한다() {
        // given
        manageSchoolPort.save(School.create("한성대", "비고1"));
        manageSchoolPort.save(School.create("동국대", "비고2"));
        manageSchoolPort.save(School.create("중앙대", "비고3"));

        // when
        List<SchoolNameInfo> result = getSchoolUseCase.getAllSchoolNames();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(SchoolNameInfo::schoolName)
                .containsExactly("동국대", "중앙대", "한성대");
    }

    @Test
    void 학교가_없으면_빈_목록을_반환한다() {
        // when
        List<SchoolNameInfo> result = getSchoolUseCase.getAllSchoolNames();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 존재하지_않는_학교_상세를_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> getSchoolUseCase.getSchoolDetail(999L))
                .isInstanceOf(BusinessException.class);
    }

    private FileMetadata saveTestFile(String fileId, String fileName) {
        FileMetadata metadata = FileMetadata.builder()
                .fileId(fileId)
                .originalFileName(fileName)
                .category(FileCategory.SCHOOL_LOGO)
                .contentType("image/png")
                .fileSize(1024L)
                .storageProvider(StorageProvider.GOOGLE_CLOUD_STORAGE)
                .storageKey("school-logo/" + fileId + ".png")
                .uploadedMemberId(1L)
                .build();

        return saveFileMetadataPort.save(metadata);
    }
}
