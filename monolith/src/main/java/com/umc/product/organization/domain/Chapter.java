package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "chapter",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_chapter_gisu_id_name", columnNames = {"gisu_id", "name"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chapter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gisu_id")
    private Gisu gisu;

    private String name;

    @Builder(access = AccessLevel.PRIVATE)
    private Chapter(Gisu gisu, String name) {
        validate(gisu, name);
        this.gisu = gisu;
        this.name = name;
    }

    public static Chapter create(Gisu gisu, String name) {
        return Chapter.builder()
            .gisu(gisu)
            .name(name)
            .build();
    }

    private static void validate(Gisu gisu, String name) {
        if (gisu == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_REQUIRED);
        }
        if (name == null || name.isBlank()) {
            throw new OrganizationDomainException(OrganizationErrorCode.ORGAN_NAME_REQUIRED);
        }
    }
}
