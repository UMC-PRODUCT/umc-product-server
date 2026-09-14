package com.umc.product.notice.application.port.in.command;

import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import java.util.List;

public interface ManageNoticeUseCase {
    /*
     * 공지 생성
     * @return 생성된 공지 ID
     */
    Long createNotice(CreateNoticeCommand command);

    /**
     * 공지 일괄 생성 (atomic batch).
     * <p>
     * 동일 트랜잭션 안에서 N 건을 순차 처리해 동일 작성자의 권한 검증 SELECT 가 1 차 캐시 활용으로
     * 1 회로 묶이고, 트랜잭션 commit 도 N → 1 회로 감소한다. 권한 검증과 알림 발송은 단건
     * {@link #createNotice} 와 동일하게 매 command 별로 수행되며, 한 건 실패 시 전체 롤백된다.
     *
     * @return 생성된 공지 ID 목록 (입력 순서 보존)
     */
    List<Long> createNoticeBulk(List<CreateNoticeCommand> commands);

    /*
     * 공지 제목 또는 내용 수정
     */
    void updateNoticeTitleOrContent(UpdateNoticeCommand command);

    /*
     * 공지 삭제
     * @return
     */
    void deleteNotice(DeleteNoticeCommand command);

    /*
     * 공지사항 리마인드 알림 보내기
     */
    void remindNotice(SendNoticeReminderCommand command);

    /*
     * 공지 조회수 증가
     */
    void incrementViewCount(Long noticeId);

}
