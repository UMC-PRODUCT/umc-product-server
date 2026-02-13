package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.application.port.in.query.dto.GetVoteDetailsQuery;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo;

public interface GetVoteDetailUseCase {
    VoteInfo get(GetVoteDetailsQuery query);
}
