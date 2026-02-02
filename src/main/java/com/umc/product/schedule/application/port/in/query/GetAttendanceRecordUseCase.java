package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.AttendanceRecordInfo;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import java.util.List;

/**
 * 출석 기록(AttendanceRecord) 조회 UseCase.
 * <p>
 * 다양한 기준으로 출석 기록을 조회한다
 * <p> - 단건 조회 (recordId)
 * <p> -출석부 기준 전체 조회 - 특정 일정의 모든 챌린저 출석 현황, 기본 기능
 * <p> - 챌린저 기준 전체 조회 - 특정 챌린저의 전체, 개인용 출석이력
 * <p> - 출석부 기준 PENDING만 조회 - 운영진을 위한 승인 대기 목록
 */
public interface GetAttendanceRecordUseCase {

    AttendanceRecordInfo getById(AttendanceRecordId recordId);

    /**
     * 특정 출석부에 속한 모든 출석 기록 조회
     */
    List<AttendanceRecordInfo> getBySheetId(Long sheetId);

    /**
     * 특정 챌린저의 전체 출석 기록 조회
     */
    List<AttendanceRecordInfo> getByChallengerId(Long challengerId);

    /**
     * 특정 출석부에서 승인 대기 중(PENDING)인 기록만 조회
     */
    List<AttendanceRecordInfo> getPendingBySheetId(Long sheetId);
}
