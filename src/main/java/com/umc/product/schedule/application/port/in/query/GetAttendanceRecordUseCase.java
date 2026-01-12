package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.dto.AttendanceRecordInfo;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import java.util.List;

/**
 * 출석 기록 조회 UseCase
 */
public interface GetAttendanceRecordUseCase {

    /**
     * 출석 기록 조회
     *
     * @param recordId 출석 기록 ID
     * @return 출석 기록 정보
     */
    AttendanceRecordInfo getById(AttendanceRecordId recordId);

    /**
     * 출석부별 출석 기록 목록 조회
     *
     * @param sheetId 출석부 ID
     * @return 출석 기록 정보 목록
     */
    List<AttendanceRecordInfo> getBySheetId(Long sheetId);

    /**
     * 챌린저별 출석 기록 목록 조회
     *
     * @param challengerId 챌린저 ID
     * @return 출석 기록 정보 목록
     */
    List<AttendanceRecordInfo> getByChallengerId(Long challengerId);

    /**
     * 승인 대기 출석 기록 목록 조회
     *
     * @param sheetId 출석부 ID
     * @return 승인 대기 출석 기록 정보 목록
     */
    List<AttendanceRecordInfo> getPendingBySheetId(Long sheetId);
}
