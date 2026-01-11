package com.umc.product.community.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Boards {
    @Getter
    private final BoardsId boardsId;

    //작가는 challenge id가져와서 쓰자 잠시기둘

    @Getter
    private String title;
    //얘네는 수정될수 있으니까..
    @Getter
    private String content;

    @Getter
    private final Category category;


    @Getter
    private final String region;

    @Getter
    private final boolean anonymous;

    @Getter
    private final LightningInfo lightningInfo;

    public static Boards createBoards(String title, String content, Category category, String region, boolean anonymous) {
        if (category == Category.LIGHTNING) {
            throw new IllegalArgumentException("번개 게시글은 createLightning()을 사용하세요");
        }
        validateCommonFields(title, content, region);
        return new Boards(null, title, content, category, region, anonymous, null);
    }

    public static Boards createLightning(String title, String content, String region, boolean anonymous, LightningInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("번개 게시글은 추가 정보가 필수입니다.");
        }
        validateCommonFields(title, content, region);
        return new Boards(null, title, content, Category.LIGHTNING, region, anonymous, info);
    }

    public static Boards reconstruct(BoardsId boardsId, String title, String content, Category category, String region,
                                   boolean anonymous, LightningInfo lightningInfo) {
        return new Boards(boardsId, title, content, category, region, anonymous, lightningInfo);
    }

    public boolean isLightning() {
        return category == Category.LIGHTNING;
    }

    public LightningInfo getLightningInfoOrThrow() {
        if (!isLightning() || lightningInfo == null) {
            throw new IllegalStateException("번개 게시글이 아닙니다.");
        }
        return lightningInfo;
    }

    private static void validateCommonFields(String title, String content, String region) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("지역은 필수입니다.");
        }
    }

    public void update(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
        new IllegalArgumentException("제목은 비어있을 수 없습니다.");

        this.title = title;
        this.content = content;

    }

    public record BoardsId(Long id) {
        public BoardsId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }

    public record LightningInfo(
            LocalDateTime meetAt,
            String location,
            Integer maxParticipants
    ) {
        public LightningInfo {
            if (meetAt == null || meetAt.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("모임 시간은 현재 이후여야 합니다.");
            }
            if (location == null || location.isBlank()) {
                throw new IllegalArgumentException("모임 장소는 필수입니다.");
            }
            if (maxParticipants <= 0) {
                throw new IllegalArgumentException("최대 참가자는 1명 이상이어야 합니다.");
            }
        }
    }

}
