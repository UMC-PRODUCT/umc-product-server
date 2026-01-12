package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import java.util.List;
import java.util.Optional;

//출석 기록 조회 파트
public interface LoadAttendanceRecordPort {

    /**
     * ID로 출석 기록 조회
     *
     * @param id 출석 기록 ID
     * @return 출석 기록
     */
    Optional<AttendanceRecord> findById(AttendanceRecordId id);

    /**
     * 출석부 ID로 출석 기록 목록 조회
     *
     * @param sheetId 출석부 ID
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByAttendanceSheetId(Long sheetId);

    /**
     * 챌린저 ID로 출석 기록 목록 조회
     *
     * @param challengerId 챌린저 ID
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByChallengerId(Long challengerId);

    /**
     * 출석부 ID + 챌린저 ID로 출석 기록 조회
     *
     * @param sheetId      출석부 ID
     * @param challengerId 챌린저 ID
     * @return 출석 기록
     */
    Optional<AttendanceRecord> findBySheetIdAndChallengerId(Long sheetId, Long challengerId);

    /**
     * 승인 대기 출석 기록 목록 조회
     *
     * @param sheetId 출석부 ID
     * @return 승인 대기 출석 기록 목록
     */
    List<AttendanceRecord> findPendingRecordsBySheetId(Long sheetId);

    /**
     * 출석 기록 존재 여부 확인
     *
     * @param sheetId      출석부 ID
     * @param challengerId 챌린저 ID
     * @return 존재 여부
     */
    boolean existsBySheetIdAndChallengerId(Long sheetId, Long challengerId);
}
