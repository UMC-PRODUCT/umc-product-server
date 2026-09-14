package com.umc.product.curriculum.application.port.in.command;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.ChangeOriginalWorkbookStatusCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateOriginalWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditOriginalWorkbookCommand;

import java.util.List;

/**
 * OriginalWorkbook 관리 UseCase
 * <p>
 * 중앙운영사무국 교육국 소속 파트장 권한 필요
 */
public interface ManageOriginalWorkbookUseCase {

    /**
     * 원본 워크북 생성
     * <p>
     * 주차별 시작 기간이 경과되지 않은 경우에만 추가 가능합니다.
     *
     * @param command 생성 커맨드 (주차별 커리큘럼 ID, 제목, 설명, URL, 내용, 유형)
     * @return 생성된 원본 워크북 ID
     */
    Long create(CreateOriginalWorkbookCommand command);

    /**
     * 원본 워크북 일괄 생성 (atomic batch).
     * <p>
     * 동일 트랜잭션 안에서 N 건을 순차 처리하므로 동일 weeklyCurriculumId 에 대한 검증 SELECT 가
     * 1 차 캐시 활용으로 1 회로 묶이고, 트랜잭션 commit 도 N → 1 회로 감소한다. 도메인 검증은 단건
     * {@link #create} 와 동일하게 매 command 별로 수행되며, 한 건 실패 시 전체 롤백된다.
     *
     * @return 생성된 원본 워크북 ID 목록 (입력 순서 보존)
     */
    List<Long> createBulk(List<CreateOriginalWorkbookCommand> commands);

    /**
     * 원본 워크북 수정
     *
     * @param command 수정 커맨드 (원본 워크북 ID, 변경할 필드들)
     */
    void edit(EditOriginalWorkbookCommand command);

    /**
     * 원본 워크북 삭제
     * <p>
     * 배포받은 챌린저가 존재하는 경우 삭제 불가능합니다.
     *
     * @param originalWorkbookId 삭제 대상 원본 워크북 ID
     */
    void delete(Long originalWorkbookId);

    /**
     * 원본 워크북 상태 일괄 변경 (배포 준비 또는 배포 처리)
     * <p>
     * 요청 중 하나라도 실패하면 전체 요청이 실패합니다.
     * - READY: 스케줄러에 의해 자동 배포 가능한 상태
     * - RELEASED: 수동 또는 자동으로 배포 완료된 상태 (READY에서만 전환 가능)
     *
     * @param commands 상태 변경 커맨드 목록 (원본 워크북 ID, 변경할 상태)
     */
    void changeStatusForRelease(List<ChangeOriginalWorkbookStatusCommand> commands);

}
