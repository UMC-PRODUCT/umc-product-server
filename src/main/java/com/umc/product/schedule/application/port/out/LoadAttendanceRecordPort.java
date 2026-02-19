package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.out.dto.AttendanceRecordPermissionContext;
import com.umc.product.schedule.domain.AttendanceRecord;
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
    Optional<AttendanceRecord> findById(Long id);

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
     * @param memberId 챌린저 ID
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByMemberId(Long memberId);

    /**
     * 출석부 ID + 챌린저 ID로 출석 기록 조회
     *
     * @param sheetId  출석부 ID
     * @param memberId 챌린저 ID
     * @return 출석 기록
     */
    Optional<AttendanceRecord> findBySheetIdAndMemberId(Long sheetId, Long memberId);

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
     * @param sheetId  출석부 ID
     * @param memberId 챌린저 ID
     * @return 존재 여부
     */
    boolean existsBySheetIdAndMemberId(Long sheetId, Long memberId);

    /**
     * 여러 출석부 ID로 출석 기록 목록 조회
     *
     * @param sheetIds 출석부 ID 목록
     * @return 출석 기록 목록
     */
    List<AttendanceRecord> findByAttendanceSheetIds(List<Long> sheetIds);

    /**
     * 승인 대기 출석 기록을 멤버 정보와 함께 조회
     *
     * @param sheetId 출석부 ID
     * @return 멤버 정보가 포함된 승인 대기 출석 정보 목록
     */
    List<PendingAttendanceInfo> findPendingWithMemberInfo(Long sheetId);

    /**
     * 여러 출석부의 승인 대기 출석 기록을 멤버 정보와 함께 일괄 조회
     *
     * @param sheetIds 출석부 ID 목록
     * @return 멤버 정보가 포함된 승인 대기 출석 정보 목록
     */
    List<PendingAttendanceInfo> findPendingWithMemberInfoBySheetIds(List<Long> sheetIds);

    /**
     * 권한 평가에 필요한 컨텍스트 정보 조회 (record → sheet → schedule JOIN)
     *
     * @param recordId 출석 기록 ID
     * @return 권한 평가 컨텍스트
     */
    Optional<AttendanceRecordPermissionContext> findPermissionContext(Long recordId);
}
