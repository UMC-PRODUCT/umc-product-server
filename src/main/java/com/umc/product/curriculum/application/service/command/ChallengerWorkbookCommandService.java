package com.umc.product.curriculum.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.DeleteChallengerWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.DeployChallengerWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.EditChallengerWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.ExcuseChallengerWorkbookCommand;
import com.umc.product.curriculum.application.port.in.query.dto.ChallengerWorkbookInfo;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveChallengerWorkbookPort;
import com.umc.product.global.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengerWorkbookCommandService implements ManageChallengerWorkbookUseCase {

    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveChallengerWorkbookPort saveChallengerWorkbookPort;
    private final GetChallengerUseCase getChallengerUseCase;

    @Override
    public List<ChallengerWorkbookInfo> deploy(DeployChallengerWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void editChallengerWorkbook(EditChallengerWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteChallengerWorkbook(DeleteChallengerWorkbookCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void excuseChallengerWorkbook(ExcuseChallengerWorkbookCommand command) {
        throw new NotImplementedException();
    }

}
