package com.umc.product.challenger.application.port.in.command;

import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerPointCommand;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerPointCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerPointCommand;

public interface ManageChallengerUseCase {

    // TODO: ChallengerRole과 관련된 부분은 추후 RBAC/ABAC 설계 후 적용 w/ 와나

    /**
     * 챌린저의 정보를 수정합니다.
     * <p>
     * 상태 변경 (제명, 자진 탈부 처리), 파트 변경이 가능합니다.
     */
    void updateChallenger(UpdateChallengerCommand command);

    /**
     * 챌린저를 잘못 생성한 경우를 위한 삭제 처리입니다.
     * <p>
     * TODO: 중앙운영사무국 CORE 전용으로 권한 관리가 필요합니다.
     */
    void deleteChallenger(DeleteChallengerCommand command);

    /**
     * 챌린저에게 상벌점을 부여합니다.
     * <p>
     * 상벌점에 대한 사유도 함께 기록이 가능합니다.
     */
    void createChallengerPoint(CreateChallengerPointCommand command);

    /**
     * 챌린저에게 부여한 상벌점에 대한 사유를 수정할 수 있습니다. 삭제의 경우 별도 기능을 이용하세요.
     * <p>
     * TODO: 권한처리가 필요합니다.
     */
    void updateChallengerPoint(UpdateChallengerPointCommand command);

    /**
     * 챌린저에게 부여한 상벌점을 삭제합니다.
     */
    void deleteChallengerPoint(DeleteChallengerPointCommand command);
}
