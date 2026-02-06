package com.umc.product.curriculum.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.in.query.CurriculumWeekInfo;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumProgressUseCase;
import com.umc.product.curriculum.application.port.out.LoadCurriculumProgressPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumQueryService implements GetCurriculumProgressUseCase {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final LoadCurriculumProgressPort loadCurriculumProgressPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;

    @Override
    public CurriculumProgressInfo getMyProgress(Long memberId) {
        Long activeGisuId = getGisuUseCase.getActiveGisuId();
        ChallengerInfo challengerInfo = getChallengerUseCase.getByMemberIdAndGisuId(memberId, activeGisuId);

        return loadCurriculumProgressPort.findCurriculumProgress(challengerInfo.challengerId(), challengerInfo.part());
    }

    @Override
    public List<CurriculumWeekInfo> getWeeksByPart(ChallengerPart part) {
        return loadOriginalWorkbookPort.findWeekInfoByActiveGisuAndPart(part);
    }
}
