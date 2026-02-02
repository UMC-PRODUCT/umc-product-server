package com.umc.product.notice.application.port.in.command;

import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;

public interface ManageNoticeUseCase {
    /*
     * 공지 임시작성, 다른 요소들(url, img, vote) 추가 후 작성완료 누르면 publish
     * @return 생성된 공지 ID
     */
    Long createNotice(CreateNoticeCommand command);

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


}
