package com.umc.product.test.application.service;

import com.umc.product.community.application.port.in.command.comment.dto.CreateCommentCommand;
import com.umc.product.community.application.port.in.command.post.dto.CreatePostCommand;
import com.umc.product.community.application.port.in.command.trophy.dto.CreateTrophyCommand;
import com.umc.product.community.domain.enums.Category;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * datafaker 를 사용해 test 도메인 시딩용 더미 Community Command 를 생성한다. ADR-017 참조.
 * <p>
 * Post 카테고리는 {@link Category#LIGHTNING} 을 제외한 일반 카테고리만 사용한다(번개는
 * LightningInfo 가 필요해 시딩 흐름이 복잡해진다). Trophy 의 week 은 1~16 범위 무작위.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DummyCommunityFactory {

    private static final int MAX_TROPHY_WEEK = 16;
    private static final int POST_CONTENT_MIN_PARAGRAPHS = 1;
    private static final int POST_CONTENT_MAX_PARAGRAPHS = 3;

    private static final List<Category> SEEDABLE_CATEGORIES = Arrays.stream(Category.values())
        .filter(c -> !c.isLightning())
        .toList();

    private final Faker faker = new Faker(Locale.KOREAN);

    /**
     * Post 생성 Command 를 만든다. 카테고리는 LIGHTNING 을 제외한 값 중 무작위.
     */
    public CreatePostCommand nextPostCommand(Long authorChallengerId) {
        Category category = SEEDABLE_CATEGORIES.get(
            ThreadLocalRandom.current().nextInt(SEEDABLE_CATEGORIES.size())
        );
        String title = faker.book().title();
        int paragraphs = POST_CONTENT_MIN_PARAGRAPHS
            + ThreadLocalRandom.current().nextInt(POST_CONTENT_MAX_PARAGRAPHS - POST_CONTENT_MIN_PARAGRAPHS + 1);
        String content = String.join("\n\n", faker.lorem().paragraphs(paragraphs));
        return new CreatePostCommand(title, content, category, authorChallengerId);
    }

    /**
     * 원댓글 생성 Command 를 만든다 (parentId = null).
     */
    public CreateCommentCommand nextCommentCommand(Long postId, Long authorChallengerId) {
        String content = faker.lorem().sentence();
        return new CreateCommentCommand(postId, authorChallengerId, content, null);
    }

    /**
     * Trophy 생성 Command 를 만든다. week 은 1~16 범위 무작위.
     */
    public CreateTrophyCommand nextTrophyCommand(Long challengerId) {
        int week = 1 + ThreadLocalRandom.current().nextInt(MAX_TROPHY_WEEK);
        String title = faker.book().title();
        String content = faker.lorem().sentence();
        String url = "https://alpha.umc.test/dummy/trophy/" + faker.internet().uuid();
        return CreateTrophyCommand.builder()
            .challengerId(challengerId)
            .week(week)
            .title(title)
            .content(content)
            .url(url)
            .build();
    }
}
