package com.umc.product.test.application.service;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTab;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * datafaker 를 사용해 test 도메인 시딩용 더미 Notice Command 를 생성한다. ADR-017 참조.
 * <p>
 * 제목·내용에는 항상 공지의 대상 범위 ([전체]/[지부]/[학교]/[파트]) 와 식별자 (지부명/학교명/파트명)
 * 가 포함되어 운영진 화면에서 시딩 데이터가 어느 카테고리에서 왔는지 한눈에 식별 가능하다.
 * <p>
 * 모든 더미 공지는 {@code shouldNotify=false} 로 생성되어 실제 알림이 발송되지 않는다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DummyNoticeFactory {

    private final Faker faker = new Faker(Locale.KOREAN);

    /**
     * 전체 기수 대상 공지 (SPECIFIC_GISU_ALL_TARGET). 작성자에게 중앙 총괄단 권한 필요.
     */
    public CreateNoticeCommand nextGlobalNoticeCommand(Long gisuId, Long authorMemberId, int seq) {
        String title = "[전체] %d기 전체 공지 #%04d: %s".formatted(gisuId, seq, faker.book().title());
        String content = "대상: %d기 전체\n\n%s".formatted(gisuId, randomBody());
        return new CreateNoticeCommand(
            authorMemberId, title, content, false, mustReadRandom(),
            new NoticeTargetInfo(gisuId, null, null, List.of(), NoticeTab.CHALLENGER)
        );
    }

    /**
     * 특정 지부 대상 공지 (SPECIFIC_GISU_SPECIFIC_CHAPTER). 작성자에게 해당 지부 회장 권한 필요.
     */
    public CreateNoticeCommand nextChapterNoticeCommand(
        Long gisuId, Long authorMemberId, Long chapterId, String chapterName, int seq
    ) {
        String title = "[지부] %d기 %s 지부 공지 #%04d: %s"
            .formatted(gisuId, chapterName, seq, faker.book().title());
        String content = "대상: %d기 / %s 지부 (chapterId=%d)\n\n%s"
            .formatted(gisuId, chapterName, chapterId, randomBody());
        return new CreateNoticeCommand(
            authorMemberId, title, content, false, mustReadRandom(),
            new NoticeTargetInfo(gisuId, chapterId, null, List.of(), NoticeTab.CHALLENGER)
        );
    }

    /**
     * 특정 학교 대상 공지 (SPECIFIC_GISU_SPECIFIC_SCHOOL). 작성자에게 해당 학교 회장단 권한 필요.
     */
    public CreateNoticeCommand nextSchoolNoticeCommand(
        Long gisuId, Long authorMemberId, Long schoolId, String schoolName, int seq
    ) {
        String title = "[학교] %d기 %s 학교 공지 #%04d: %s"
            .formatted(gisuId, schoolName, seq, faker.book().title());
        String content = "대상: %d기 / %s 학교 (schoolId=%d)\n\n%s"
            .formatted(gisuId, schoolName, schoolId, randomBody());
        return new CreateNoticeCommand(
            authorMemberId, title, content, false, mustReadRandom(),
            new NoticeTargetInfo(gisuId, null, schoolId, List.of(), NoticeTab.CHALLENGER)
        );
    }

    /**
     * 특정 파트 대상 공지 (SPECIFIC_GISU_SPECIFIC_PART). 작성자에게 중앙 운영진 권한 필요.
     */
    public CreateNoticeCommand nextPartNoticeCommand(
        Long gisuId, Long authorMemberId, ChallengerPart part, int seq
    ) {
        String title = "[파트] %d기 %s 파트 공지 #%04d: %s"
            .formatted(gisuId, part.name(), seq, faker.book().title());
        String content = "대상: %d기 / %s 파트\n\n%s"
            .formatted(gisuId, part.name(), randomBody());
        return new CreateNoticeCommand(
            authorMemberId, title, content, false, mustReadRandom(),
            new NoticeTargetInfo(gisuId, null, null, List.of(part), NoticeTab.CHALLENGER)
        );
    }

    private String randomBody() {
        return String.join("\n\n", faker.lorem().paragraphs(2));
    }

    /**
     * 절반 확률로 필독 처리해 운영 화면의 필독/일반 표시가 모두 분포되도록 한다.
     */
    private boolean mustReadRandom() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
