package com.umc.product.notice.adapter.in.web.dto.response;

import java.util.List;

public record AddNoticeVotesResponse(
    List<Long> voteIds
) {
}
