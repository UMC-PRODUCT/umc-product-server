package com.umc.product.notice.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.enums.UpmsNoticeType;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@ActiveProfiles("test")
@DisplayName("UPMS 공지 유형 저장 및 조회")
class NoticeUpmsNoticeTypePersistenceTest {

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("UPMS 유형을 지정하지 않고 생성한 공지는 유형을 null로 저장한다")
    void upms_유형_미지정_공지_null_저장() {
        // given
        Notice notice = Notice.create("제목", "내용", 1L, false, false);

        // when
        Long noticeId = em.persistAndFlush(notice).getId();
        em.clear();

        // then
        assertThat(em.find(Notice.class, noticeId).getUpmsNoticeType()).isNull();
    }

    @ParameterizedTest
    @EnumSource(UpmsNoticeType.class)
    @DisplayName("UPMS 유형을 DB에 문자열로 저장하고 동일한 enum으로 조회한다")
    void upms_유형_문자열_저장과_enum_조회(UpmsNoticeType type) {
        // given
        Notice notice = Notice.create("제목", "내용", 1L, false, false, type);

        // when
        Long noticeId = em.persistAndFlush(notice).getId();
        em.clear();
        Notice savedNotice = em.find(Notice.class, noticeId);
        Object storedType = em.getEntityManager()
            .createNativeQuery("SELECT upms_notice_type FROM notice WHERE id = :id")
            .setParameter("id", noticeId)
            .getSingleResult();

        // then
        assertThat(savedNotice.getUpmsNoticeType()).isEqualTo(type);
        assertThat(storedType).isEqualTo(type.name());
    }
}
