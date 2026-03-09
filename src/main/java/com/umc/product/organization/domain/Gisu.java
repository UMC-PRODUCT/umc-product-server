package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.domain.vo.GisuPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "gisu")
public class Gisu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long generation;

    @Column(name = "is_active")
    private boolean isActive;

    @Embedded
    private GisuPeriod period;

    @Builder(access = AccessLevel.PRIVATE)
    private Gisu(Long generation, GisuPeriod period, boolean isActive) {
        this.generation = generation;
        this.isActive = isActive;
        this.period = period;
    }

    public static Gisu create(Long generation, Instant startAt, Instant endAt, boolean isActive) {
        return new Gisu(generation, GisuPeriod.of(startAt, endAt), isActive);
    }

    public Instant getStartAt() {
        return period.getStartAt();
    }

    public Instant getEndAt() {
        return period.getEndAt();
    }

    public void active() {
        this.isActive = true;
    }

    public void inactive() {
        this.isActive = false;
    }
}
