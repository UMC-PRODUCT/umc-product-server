package com.umc.product.notice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.notice.domain.enums.NoticeTargetPattern;

@ExtendWith(MockitoExtension.class)
class NoticeTargetPatternTest {

    @Mock GetChallengerRoleUseCase roles;

    @Test
    void 대상_기수와_무관하게_활성_기수의_작성_역할을_확인한다() {
        // given
        NoticeTargetInfo target = new NoticeTargetInfo(9L, null, null, List.of(), NoticeTab.CHALLENGER);
        given(roles.isCentralMemberInGisu(1L, 10L)).willReturn(true);
        // when
        boolean result = NoticeTargetPattern.from(target).validatePermissionInGisu(target, 1L, roles, 10L);
        // then
        assertThat(result).isTrue();
        verify(roles, never()).isCentralMemberInGisu(1L, 9L);
    }

    @Test
    void 과거_총괄단_역할만으로_전체_기수_공지를_작성할_수_없다() {
        // given
        NoticeTargetInfo target = new NoticeTargetInfo(null, null, null, List.of(), NoticeTab.CHALLENGER);
        // when
        boolean result = NoticeTargetPattern.from(target).validatePermissionInGisu(target, 1L, roles, 10L);
        // then
        assertThat(result).isFalse();
        verify(roles).isCentralCoreInGisu(1L, 10L);
        verify(roles, never()).isCentralCoreInAnyGisu(1L);
    }

    @Test
    void 교내_운영진은_담당_파트와_무관하게_파트_운영진_공지를_작성한다() {
        // given
        NoticeTargetInfo target = new NoticeTargetInfo(10L, null, 5L,
            List.of(ChallengerPart.MOBILE_PRODUCT_ENGINEER), NoticeTab.SCHOOL_PART_LEADER);
        given(roles.isSchoolAdminInGisu(1L, 10L, 5L)).willReturn(true);
        // when
        boolean result = NoticeTargetPattern.from(target).validatePermissionInGisu(target, 1L, roles, 10L);
        // then
        assertThat(result).isTrue();
        verify(roles, never()).isSchoolCoreInGisu(1L, 10L, 5L);
    }

    @Test
    void 파트_미지정_학교_전체_공지는_회장단만_작성한다() {
        // given
        NoticeTargetInfo target = new NoticeTargetInfo(10L, null, 5L, List.of(), NoticeTab.SCHOOL_PART_LEADER);
        // when
        boolean result = NoticeTargetPattern.from(target).validatePermissionInGisu(target, 1L, roles, 10L);
        // then
        assertThat(result).isFalse();
        verify(roles).isSchoolCoreInGisu(1L, 10L, 5L);
        verify(roles, never()).isSchoolAdminInGisu(1L, 10L, 5L);
    }
}
