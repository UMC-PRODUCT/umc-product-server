package com.umc.product.analytics.adapter.in.web.dto.response;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AdminOperationsOverviewResponse(
    List<ChapterSchoolStatusResponse> chapterSchoolStatuses,
    List<ChapterPartPointGrantStatusResponse> pointGrantStatuses,
    ScheduleAttendanceStatusResponse scheduleAttendanceStatus,
    StudyGroupStatusResponse studyGroupStatus,
    List<SignupBucketResponse> signupBuckets
) {

    public static AdminOperationsOverviewResponse from(AdminOperationsOverviewInfo info) {
        return new AdminOperationsOverviewResponse(
            info.chapterSchoolStatuses().stream()
                .map(ChapterSchoolStatusResponse::from)
                .toList(),
            info.pointGrantStatuses().stream()
                .map(ChapterPartPointGrantStatusResponse::from)
                .toList(),
            ScheduleAttendanceStatusResponse.from(info.scheduleAttendanceStatus()),
            StudyGroupStatusResponse.from(info.studyGroupStatus()),
            info.signupBuckets().stream()
                .map(SignupBucketResponse::from)
                .toList()
        );
    }

    public record ChapterSchoolStatusResponse(
        Long chapterId,
        String chapterName,
        List<SchoolChallengerStatusResponse> schools
    ) {

        public static ChapterSchoolStatusResponse from(
            AdminOperationsOverviewInfo.ChapterSchoolStatusInfo info
        ) {
            return new ChapterSchoolStatusResponse(
                info.chapterId(),
                info.chapterName(),
                info.schools().stream()
                    .map(SchoolChallengerStatusResponse::from)
                    .toList()
            );
        }
    }

    public record SchoolChallengerStatusResponse(
        Long schoolId,
        String schoolName,
        long totalChallengerCount,
        Map<ChallengerPart, Long> challengerPartCounts
    ) {

        public static SchoolChallengerStatusResponse from(
            AdminOperationsOverviewInfo.SchoolChallengerStatusInfo info
        ) {
            return new SchoolChallengerStatusResponse(
                info.schoolId(),
                info.schoolName(),
                info.totalChallengerCount(),
                info.challengerPartCounts()
            );
        }
    }

    public record ChapterPartPointGrantStatusResponse(
        Long chapterId,
        String chapterName,
        ChallengerPart part,
        long grantCount,
        double pointSum
    ) {

        public static ChapterPartPointGrantStatusResponse from(
            AdminOperationsOverviewInfo.ChapterPartPointGrantStatusInfo info
        ) {
            return new ChapterPartPointGrantStatusResponse(
                info.chapterId(),
                info.chapterName(),
                info.part(),
                info.grantCount(),
                info.pointSum()
            );
        }
    }

    public record ScheduleAttendanceStatusResponse(
        long scheduleCount,
        long attendanceRequiredScheduleCount,
        long attendanceRecordCount,
        Map<AttendanceStatus, Long> attendanceStatusCounts
    ) {

        public static ScheduleAttendanceStatusResponse from(
            AdminOperationsOverviewInfo.ScheduleAttendanceStatusInfo info
        ) {
            return new ScheduleAttendanceStatusResponse(
                info.scheduleCount(),
                info.attendanceRequiredScheduleCount(),
                info.attendanceRecordCount(),
                info.attendanceStatusCounts()
            );
        }
    }

    public record StudyGroupStatusResponse(
        long studyGroupCount,
        long studyGroupScheduleCount
    ) {

        public static StudyGroupStatusResponse from(AdminOperationsOverviewInfo.StudyGroupStatusInfo info) {
            return new StudyGroupStatusResponse(info.studyGroupCount(), info.studyGroupScheduleCount());
        }
    }

    public record SignupBucketResponse(
        LocalDate date,
        long count
    ) {

        public static SignupBucketResponse from(AdminOperationsOverviewInfo.SignupBucketInfo info) {
            return new SignupBucketResponse(info.date(), info.count());
        }
    }
}
