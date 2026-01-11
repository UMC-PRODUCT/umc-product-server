package com.umc.product.notice.application.port.in.command;

import java.util.List;

/*
* 공지의 본체 외 이미지, 투표, 링크 관리
* */
public interface ManageNoticeContentUseCase {
    List<Long> addVote(AddNoticeVoteCommand command);
    List<Long> addImages(AddNoticeImagesCommand command);
    List<Long> addLinks(AddNoticeLinksCommand command);
    void removeVote(RemoveNoticeVoteCommand command);
    void removeImage(RemoveNoticeImageCommand command);
    void removeLink(RemoveNoticeLinkCommand command);
}
