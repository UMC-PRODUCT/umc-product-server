package com.umc.product.curriculum.application.service.query;

import com.umc.product.curriculum.application.port.in.query.GetChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.ChallengerWorkbookInfo;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadWorkbookSubmissionPort;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerWorkbookQueryService implements GetChallengerWorkbookUseCase {

    private final LoadWorkbookSubmissionPort loadWorkbookSubmissionPort;
    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final GetStudyGroupUseCase getStudyGroupUseCase;
    private final GetFileUseCase getFileUseCase;

    @Override
    public ChallengerWorkbookInfo getById(Long challengerWorkbookId) {
        throw new NotImplementedException();
    }

}
