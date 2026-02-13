package com.umc.product.notice.application.port.in.command;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeImagesCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteResult;
import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeImagesCommand;
import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeLinksCommand;
import java.util.List;

/*
 * 공지의 본체 외 이미지, 투표, 링크 관리
 */
public interface ManageNoticeContentUseCase {

    /*
     * 공지에 투표 추가
     * 투표를 생성하고 공지에 연결
     */
    AddNoticeVoteResult addVote(AddNoticeVoteCommand command, Long noticeId);

    /*
     * 공지에 이미지 추가
     * @return 생성된 NoticeImage의 id 리스트
     */
    List<Long> addImages(AddNoticeImagesCommand command, Long noticeId);

    /*
     * 공지에 링크 추가
     * @return 생성된 NoticeLink의 id 리스트
     */
    List<Long> addLinks(AddNoticeLinksCommand command, Long noticeId);

    void removeContentsByNoticeId(Long noticeId, Long memberId);

    void replaceImages(ReplaceNoticeImagesCommand command, Long noticeId);

    void replaceLinks(ReplaceNoticeLinksCommand command, Long noticeId);
}
