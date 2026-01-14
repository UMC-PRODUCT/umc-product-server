package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.domain.Boards;
import com.umc.product.community.domain.Enum.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardsJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private boolean anonymous;

    // Lightning 관련 필드
    private LocalDateTime lightningMeetAt;
    private String lightningLocation;
    private Integer lightningMaxParticipants;

    // Domain -> JPA Entity
    public static BoardsJpaEntity from(Boards boards) {
        BoardsJpaEntity entity = new BoardsJpaEntity();
        if (boards.getBoardsId() != null) {
            entity.id = boards.getBoardsId().id();
        }
        entity.title = boards.getTitle();
        entity.content = boards.getContent();
        entity.category = boards.getCategory();
        entity.region = boards.getRegion();
        entity.anonymous = boards.isAnonymous();

        // Lightning 매핑
        if (boards.isLightning() && boards.getLightningInfo() != null) {
            Boards.LightningInfo info = boards.getLightningInfo();
            entity.lightningMeetAt = info.meetAt();
            entity.lightningLocation = info.location();
            entity.lightningMaxParticipants = info.maxParticipants();
        }

        return entity;
    }

    public Boards toDomain() {
        if (category == Category.LIGHTNING && lightningMeetAt != null) {
            Boards.LightningInfo lightningInfo = new Boards.LightningInfo(
                    lightningMeetAt,
                    lightningLocation,
                    lightningMaxParticipants
            );
            return Boards.createLightning(title, content, region, anonymous, lightningInfo);
        } else {
            return Boards.createBoards(title, content, category, region, anonymous);
        }
    }

    // ID를 포함한 Domain 객체로 변환
    public Boards toDomainWithId() {
        Boards.BoardsId boardsId = new Boards.BoardsId(this.id);

        Boards.LightningInfo lightningInfo = null;
        if (category == Category.LIGHTNING && lightningMeetAt != null) {
            lightningInfo = new Boards.LightningInfo(lightningMeetAt, lightningLocation, lightningMaxParticipants);
        }

        return Boards.reconstruct(boardsId, title, content, category, region, anonymous, lightningInfo);
    }
}
