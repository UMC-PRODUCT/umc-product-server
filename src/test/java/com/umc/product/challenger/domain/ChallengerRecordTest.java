package com.umc.product.challenger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

@DisplayName("ChallengerRecord 도메인")
class ChallengerRecordTest {

    @Test
    @DisplayName("일반 코드는 단일 파트와 infra 정보를 가진다")
    void 일반_코드는_단일_파트와_infra_정보를_가진다() {
        ChallengerRecord record = ChallengerRecord.create(
            1L, 9L, 2L, 3L, ChallengerPart.WEB_PRODUCT_ENGINEER, true, "홍길동");

        assertThat(record.getCode()).hasSize(6);
        assertThat(record.getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(record.isInfra()).isTrue();
        assertThat(record.isUsed()).isFalse();
    }

    @Test
    @DisplayName("수강하지 않는 중앙 운영진은 파트와 지부를 생략할 수 있다")
    void 수강하지_않는_중앙_운영진은_파트와_지부를_생략할_수_있다() {
        ChallengerRecord record = ChallengerRecord.createAdmin(
            1L, 9L, null, 3L, null, "홍길동",
            ChallengerRoleType.CENTRAL_PRESIDENT, null);

        record.validateLearningSelection();

        assertThat(record.isAdminRecord()).isTrue();
        assertThat(record.canOmitChapter()).isTrue();
    }

    @Test
    @DisplayName("역할 없는 코드는 파트를 생략할 수 없다")
    void 역할_없는_코드는_파트를_생략할_수_없다() {
        assertThatThrownBy(() -> ChallengerRecord.create(1L, 9L, 2L, 3L, null, "홍길동"))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST);
    }

    @Test
    @DisplayName("infra는 웹과 모바일 프로덕트 엔지니어 코드에만 지정할 수 있다")
    void infra는_개발_파트_코드에만_지정할_수_있다() {
        assertThatThrownBy(() -> ChallengerRecord.create(
            1L, 9L, 2L, 3L, ChallengerPart.DESIGN, true, "홍길동"))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST);
    }

    @Test
    @DisplayName("사용 처리 시 사용 회원과 시각을 기록한다")
    void 사용_처리_시_사용_회원과_시각을_기록한다() {
        ChallengerRecord record = ChallengerRecord.create(1L, 9L, 2L, 3L, ChallengerPart.WEB, "홍길동");

        record.markAsUsed(100L);

        assertThat(record.getUsedMemberId()).isEqualTo(100L);
        assertThat(record.getUsedAt()).isNotNull();
        assertThatThrownBy(() -> record.markAsUsed(101L))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.USED_CHALLENGER_RECORD_CODE);
    }

    @Test
    @DisplayName("기록의 회원 이름과 학교가 요청자 정보와 일치해야 한다")
    void 기록의_회원_이름과_학교가_요청자_정보와_일치해야_한다() {
        ChallengerRecord record = ChallengerRecord.create(1L, 9L, 2L, 3L, ChallengerPart.WEB, "홍길동");

        record.validateMember("홍길동", 3L);

        assertThatThrownBy(() -> record.validateMember("김철수", 3L))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_MEMBER_NAME_FOR_RECORD);
        assertThatThrownBy(() -> record.validateMember("홍길동", 4L))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_SCHOOL_FOR_RECORD);
    }
}
