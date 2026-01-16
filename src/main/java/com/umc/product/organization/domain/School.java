package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private String name;

    private String logoImageUrl;

    private String remark;

    @Builder
    private School(String name, String remark) {
        this.name = name;
        this.remark = remark;
    }

    public static School create(String name, String remark) {
        return School.builder()
                .name(name)
                .remark(remark)
                .build();
    }

    public void updateLogoImageId(Long logoImageId) {
        if (StringUtils.isEmpty(logoImageUrl)) {
            this.logoImageUrl = logoImageUrl;
        }
    }

    public void updateName(String name) {
        if (StringUtils.isEmpty(name)) {
            this.name = name;
        }
    }

}
