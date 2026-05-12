package com.umc.product.analytics.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AdminOperationsOverviewInfo(
    List<ChapterSchoolStatusInfo> chapterSchoolStatuses,
    List<ChapterPartPointGrantStatusInfo> pointGrantStatuses,
    ScheduleAttendanceStatusInfo scheduleAttendanceStatus,
    StudyGroupStatusInfo studyGroupStatus,
    List<SignupBucketInfo> signupBuckets
) {

    public static AdminOperationsOverviewInfo of(
        List<ChapterSchoolStatusInfo> chapterSchoolStatuses,
        List<ChapterPartPointGrantStatusInfo> pointGrantStatuses,
        ScheduleAttendanceStatusInfo scheduleAttendanceStatus,
        StudyGroupStatusInfo studyGroupStatus,
        List<SignupBucketInfo> signupBuckets
    ) {
        return new AdminOperationsOverviewInfo(
            List.copyOf(chapterSchoolStatuses),
            List.copyOf(pointGrantStatuses),
            scheduleAttendanceStatus,
            studyGroupStatus,
            List.copyOf(signupBuckets)
        );
    }

    public record ChapterSchoolStatusInfo(
        Long chapterId,
        String chapterName,
        List<SchoolChallengerStatusInfo> schools
    ) {

        public static ChapterSchoolStatusInfo of(
            Long chapterId,
            String chapterName,
            List<SchoolChallengerStatusInfo> schools
        ) {
            return new ChapterSchoolStatusInfo(chapterId, chapterName, List.copyOf(schools));
        }
    }

    public record SchoolChallengerStatusInfo(
        Long schoolId,
        String schoolName,
        long totalChallengerCount,
        Map<ChallengerPart, Long> challengerPartCounts
    ) {

        public static SchoolChallengerStatusInfo of(
            Long schoolId,
            String schoolName,
            long totalChallengerCount,
            Map<ChallengerPart, Long> challengerPartCounts
        ) {
            return new SchoolChallengerStatusInfo(
                schoolId,
                schoolName,
                totalChallengerCount,
                Map.copyOf(challengerPartCounts)
            );
        }
    }

    public record ChapterPartPointGrantStatusInfo(
        Long chapterId,
        String chapterName,
        ChallengerPart part,
        long grantCount,
        double pointSum
    ) {

        public static ChapterPartPointGrantStatusInfo of(
            Long chapterId,
            String chapterName,
            ChallengerPart part,
            long grantCount,
            double pointSum
        ) {
            return new ChapterPartPointGrantStatusInfo(chapterId, chapterName, part, grantCount, pointSum);
        }
    }

    public record ScheduleAttendanceStatusInfo(
        long scheduleCount,
        long attendanceRequiredScheduleCount,
        long attendanceRecordCount,
        Map<AttendanceStatus, Long> attendanceStatusCounts
    ) {

        public static ScheduleAttendanceStatusInfo of(
            long scheduleCount,
            long attendanceRequiredScheduleCount,
            long attendanceRecordCount,
            Map<AttendanceStatus, Long> attendanceStatusCounts
        ) {
            return new ScheduleAttendanceStatusInfo(
                scheduleCount,
                attendanceRequiredScheduleCount,
                attendanceRecordCount,
                Map.copyOf(attendanceStatusCounts)
            );
        }
    }

    public record StudyGroupStatusInfo(
        long studyGroupCount,
        long studyGroupScheduleCount
    ) {

        public static StudyGroupStatusInfo of(long studyGroupCount, long studyGroupScheduleCount) {
            return new StudyGroupStatusInfo(studyGroupCount, studyGroupScheduleCount);
        }
    }

    public record SignupBucketInfo(
        LocalDate date,
        long count
    ) {

        public static SignupBucketInfo of(LocalDate date, long count) {
            return new SignupBucketInfo(date, count);
        }
    }
}
