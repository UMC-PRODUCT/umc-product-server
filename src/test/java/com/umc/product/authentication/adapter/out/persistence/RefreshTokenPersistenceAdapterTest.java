package com.umc.product.authentication.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.umc.product.authentication.domain.RefreshToken;
import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.support.TestContainersConfig;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QueryDslConfig.class, TestContainersConfig.class, RefreshTokenPersistenceAdapter.class})
@DisplayName("RefreshTokenPersistenceAdapter")
class RefreshTokenPersistenceAdapterTest {

    private static final Long MEMBER_ID = 1L;
    private static final UUID JTI = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    TestEntityManager em;

    @Autowired
    RefreshTokenPersistenceAdapter adapter;

    @Test
    @DisplayName("RefreshToken을 저장하고 jti로 조회한다")
    void refresh_token_저장_후_조회() {
        // given
        RefreshToken refreshToken = RefreshToken.create(JTI, MEMBER_ID, Instant.now().plusSeconds(3600));

        // when
        adapter.save(refreshToken);
        em.flush();
        em.clear();

        // then
        Optional<RefreshToken> found = adapter.findByJti(JTI);
        assertThat(found).isPresent();
        assertThat(found.get().getMemberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("jti로 RefreshToken을 삭제하며 이미 없어도 멱등하게 성공한다")
    void refresh_token_jti_삭제_멱등() {
        // given
        adapter.save(RefreshToken.create(JTI, MEMBER_ID, Instant.now().plusSeconds(3600)));
        em.flush();
        em.clear();

        // when
        boolean firstDeleted = adapter.deleteByJti(JTI);
        boolean secondDeleted = adapter.deleteByJti(JTI);
        em.flush();
        em.clear();

        // then
        assertThat(firstDeleted).isTrue();
        assertThat(secondDeleted).isFalse();
        assertThat(adapter.findByJti(JTI)).isEmpty();
    }
}
