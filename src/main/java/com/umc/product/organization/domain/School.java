package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class School extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "school", orphanRemoval = true, cascade = CascadeType.ALL)
    ArrayList<ChapterSchool> chapterSchools;

    private String name;

    private String logoImageUrl;

    private String remark;

    @Builder
    private School(String name, String remark, ArrayList<ChapterSchool> chapterSchools) {
        this.name = name;
        this.remark = remark;
        this.chapterSchools = chapterSchools;
    }

    public static School create(String name, String remark) {
        return School.builder()
                .name(name)
                .remark(remark)
                .chapterSchools(new ArrayList<>())
                .build();
    }

    public void updateLogoImageUrl(String logoImageUrl) {
        if (StringUtils.hasText(logoImageUrl)) {
            this.logoImageUrl = logoImageUrl;
        }
    }

    public void updateName(String name) {
        if (StringUtils.hasText(name)) {
            this.name = name;
        }
    }

    public void updateRemark(String remark) {
        if (StringUtils.hasText(remark)) {
            this.remark = remark;
        }
    }

    public void updateChapterSchool(Chapter newChapter) {
        this.chapterSchools.removeIf(cs -> cs.getChapter().getGisu().isActive());
        ChapterSchool chapterSchool = ChapterSchool.create(newChapter, this);
        this.chapterSchools.add(chapterSchool);
    }
}
