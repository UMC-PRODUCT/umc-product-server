package com.umc.product.community.application.port.in.query.thread;

import java.util.List;

import com.umc.product.community.application.port.in.query.thread.dto.ThreadMemberStatusInfo;

public interface ListCommunityThreadMemberStatusUseCase {

    List<ThreadMemberStatusInfo> listMemberStatus(Long threadId);
}
