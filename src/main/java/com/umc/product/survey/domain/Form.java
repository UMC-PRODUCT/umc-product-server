package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "form")
public class Form extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_member_id", nullable = false)
    private Long createdMemberId;

    @Column()
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isActive;
    // 여기 수정 active말고 published 이것도

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo ASC")
    private List<FormSection> sections = new ArrayList<>();

    public static Form createDraft(Long createdMemberId) {
        Form form = new Form();
        form.createdMemberId = createdMemberId;
        form.isActive = false;
        return form;
    }
}
