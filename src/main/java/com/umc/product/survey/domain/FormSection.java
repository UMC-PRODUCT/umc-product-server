package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.survey.domain.enums.FormSectionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
// TODO: Builder는 엔티티 단에서 정팩메에 붙여서 사용하도록 하고, 따로 엔티티 단에 붙이지 않는게 어떤지
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "form_section")
public class FormSection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormSectionType type;
    // TODO: orderNo 기준으로 동작하도록 함, 별도로 Default/Custom 구분은 하지 않음
    // 특정 도메인에서 필요 시 해당 도메인에서 엔티티로 관리하도록 함.

    @Column(name = "target_key", length = 50)
    private String targetKey;
    // TODO: 필요하다면 사용하는 도메인에서 엔티티로 관리하도록 함.

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "order_no")
    private Integer orderNo;
    // TODO: Long 박으면 어떨까 하는데 ..

    @OneToMany(mappedBy = "formSection", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo ASC")
    @Builder.Default
    private Set<Question> questions = new LinkedHashSet<>();
    // TODO: OneToMany 말고, ManyToOne으로 단방향 의존성으로 관리해주세요

    public static FormSection create(
        Form form,
        FormSectionType type,
        String targetKey,
        String title,
        int orderNo
    ) {
        return FormSection.builder()
            .form(form)
            .type(type)
            .targetKey(targetKey)
            .title(title)
            .orderNo(orderNo)
            .build();
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
        question.assignTo(this);
    }

}
