package com.umc.product.challenger.application.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.authentication.application.port.in.query.GetMemberOAuthUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.command.ManageChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ConsumeChallengerRecordCommand;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.LoadChallengerRecordPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerRecordPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.challenger.domain.exception.OtherAccountHasChallengerHistoryException;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.global.util.EmailMasker;
import com.umc.product.member.application.port.in.command.LockMemberUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.MemberLoginAccountInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChallengerRecordCommandService implements ManageChallengerRecordUseCase {

    private final SaveChallengerRecordPort saveChallengerRecordPort;
    private final LoadChallengerRecordPort loadChallengerRecordPort;

    private final SaveChallengerPort saveChallengerPort;
    private final LoadChallengerPort loadChallengerPort;

    private final GetChapterUseCase getChapterUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetSchoolUseCase getSchoolUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final LockMemberUseCase lockMemberUseCase;
    private final ListChallengerRoleUseCase listChallengerRoleUseCase;
    private final ManageChallengerRoleUseCase manageChallengerRoleUseCase;
    private final EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    private final GetMemberOAuthUseCase getMemberOAuthUseCase;

    @Audited(
        domain = Domain.CHALLENGER,
        action = AuditAction.CREATE,
        targetType = "ChallengerRecord",
        targetId = "#result",
        description = "'ChallengerRecord를 생성했습니다.'"
    )
    @Override
    public Long create(CreateChallengerRecordCommand command) {
        ChallengerRecord savedRecord = saveChallengerRecordPort.save(createValidatedRecord(command));
        log.info("ChallengerRecord를 생성했습니다: recordId={}, gisuId={}, schoolId={}, chapterId={}, adminRecord={}",
            savedRecord.getId(), command.gisuId(), command.schoolId(), command.chapterId(),
            command.challengerRoleType() != null);
        return savedRecord.getId();
    }

    @Audited(
        domain = Domain.CHALLENGER,
        action = AuditAction.CREATE,
        targetType = "ChallengerRecord",
        description = "'ChallengerRecord를 대량 생성했습니다. count=' + #result.size()"
    )
    @Override
    public List<Long> createBulk(List<CreateChallengerRecordCommand> commands) {
        List<ChallengerRecord> records = commands.stream()
            .map(this::createValidatedRecord)
            .toList();

        List<ChallengerRecord> savedRecords = saveChallengerRecordPort.saveAll(records);
        log.info("ChallengerRecord를 대량 생성했습니다: count={}", savedRecords.size());
        return savedRecords.stream().map(ChallengerRecord::getId).toList();
    }

    @Audited(
        domain = Domain.CHALLENGER,
        action = AuditAction.DELETE,
        targetType = "ChallengerRecord",
        targetId = "#id",
        description = "'ChallengerRecord를 삭제했습니다.'"
    )
    @Override
    public void delete(Long id) {
        saveChallengerRecordPort.delete(loadChallengerRecordPort.getById(id));
    }

    @Audited(
        domain = Domain.CHALLENGER,
        action = AuditAction.CHECK,
        targetType = "ChallengerRecord",
        targetId = "#command.targetMemberId()",
        description = "'ChallengerRecord 코드를 사용했습니다.'"
    )
    @Override
    public void consumeCode(ConsumeChallengerRecordCommand command) {
        Long memberId = command.targetMemberId();
        // 서로 다른 코드도 회원 단위로 직렬화한 뒤 최신 소속과 수강 목록을 조회한다.
        lockMemberUseCase.lockById(memberId);
        ChallengerRecord record = loadChallengerRecordPort.getByCodeForUpdate(command.code());
        record.validateNotUsed();

        getGisuUseCase.getById(record.getGisuId());
        record.validateLearningSelection();
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);
        record.validateMember(memberInfo.name(), memberInfo.schoolId());
        validateRecord(record);
        rejectIfOtherAccountHasChallengerHistory(memberId, record);

        Optional<Challenger> existing = loadChallengerPort.findByMemberIdAndGisuId(memberId, record.getGisuId());
        existing.ifPresent(Challenger::validateChallengerStatus);

        boolean membershipAdded = existing.isEmpty();
        Challenger challenger = existing.orElseGet(() -> newMembership(record, memberId));
        boolean learningAdded = !membershipAdded && challenger.applyLearning(record.getPart(), record.isInfra());
        boolean roleAdded = record.isAdminRecord() && !hasRole(record, memberId);
        if (!membershipAdded && !learningAdded && !roleAdded) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_ALREADY_EXISTS,
                "코드의 수강과 운영진 역할이 모두 이미 등록되어 있습니다.");
        }

        if (membershipAdded || learningAdded) {
            challenger = saveChallengerPort.save(challenger);
        }
        if (roleAdded) {
            manageChallengerRoleUseCase.createChallengerRole(CreateChallengerRoleCommand.builder()
                .challengerId(challenger.getId())
                .roleType(record.getChallengerRoleType())
                .organizationId(record.getOrganizationId())
                .responsiblePart(record.getPart())
                .gisuId(record.getGisuId())
                .build());
        }
        record.markAsUsed(memberId);
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
    }

    private void rejectIfOtherAccountHasChallengerHistory(Long memberId, ChallengerRecord record) {
        boolean memberAlreadyHasChallengerHistory = loadChallengerPort.existsByMemberId(memberId);
        if (memberAlreadyHasChallengerHistory) {
            return;
        }
        Map<Long, MemberLoginAccountInfo> otherAccounts = getMemberUseCase
            .listLoginAccountsByNameAndSchoolId(record.getMemberName(), record.getSchoolId()).stream()
            .filter(account -> !account.memberId().equals(memberId) && account.status() != MemberStatus.WITHDRAWN)
            .collect(Collectors.toMap(MemberLoginAccountInfo::memberId, Function.identity()));
        if (otherAccounts.isEmpty()) {
            return;
        }
        Set<Long> historyOwnerIds = loadChallengerPort.listAllByMemberIds(otherAccounts.keySet()).stream()
            .map(Challenger::getMemberId)
            .collect(Collectors.toCollection(TreeSet::new));
        if (historyOwnerIds.isEmpty()) {
            return;
        }
        Map<Long, Set<OAuthProvider>> providersByMemberId = getMemberOAuthUseCase.listByMemberIds(historyOwnerIds)
            .stream()
            .collect(Collectors.groupingBy(MemberOAuthInfo::memberId, Collectors.mapping(MemberOAuthInfo::provider,
                Collectors.toCollection(() -> EnumSet.noneOf(OAuthProvider.class)))));
        List<MemberLoginAccountInfo> accountsWithHistory = historyOwnerIds.stream().map(otherAccounts::get).toList();
        throw new OtherAccountHasChallengerHistoryException(
            otherAccountGuide(accountsWithHistory, providersByMemberId));
    }

    private static String otherAccountGuide(
        List<MemberLoginAccountInfo> accounts, Map<Long, Set<OAuthProvider>> providersByMemberId
    ) {
        String accountList = accounts.stream()
            .map(account -> {
                String loginMethods = Stream.concat(
                        providersByMemberId.getOrDefault(account.memberId(), Set.of()).stream()
                            .map(ChallengerRecordCommandService::providerName),
                        account.hasPassword() ? Stream.of("이메일") : Stream.empty())
                    .collect(Collectors.joining("·"));
                return (loginMethods.isEmpty() ? "" : loginMethods + "로 ")
                    + "가입한 계정(" + EmailMasker.mask(account.email()) + ")";
            })
            .collect(Collectors.joining(", "));
        return accountList + "에 활동 기록이 있어요. 그 계정으로 로그인해서 코드를 입력해주세요. 본인 계정이 아니면 운영진에게 문의해주세요.";
    }

    private static String providerName(OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> "카카오";
            case GOOGLE -> "구글";
            case APPLE -> "애플";
        };
    }

    private Challenger newMembership(ChallengerRecord record, Long memberId) {
        // 파트 없는 코드(예: 순수 중앙 운영진)는 수강 없는 소속으로, 파트가 있으면 파트 + infra로 생성한다.
        if (record.getPart() == null) {
            return Challenger.createWithoutEnrollment(memberId, record.getGisuId());
        }
        return Challenger.builder()
            .memberId(memberId)
            .gisuId(record.getGisuId())
            .part(record.getPart())
            .infra(record.isInfra())
            .build();
    }

    private boolean hasRole(ChallengerRecord record, Long memberId) {
        return listChallengerRoleUseCase.listByMemberIdAndGisuId(memberId, record.getGisuId()).stream()
            .anyMatch(role -> role.roleType() == record.getChallengerRoleType()
                && Objects.equals(role.organizationId(), record.getOrganizationId())
                && (record.getChallengerRoleType() != ChallengerRoleType.SCHOOL_PART_LEADER
                    || role.responsiblePart() == record.getPart()));
    }

    private ChallengerRecord createValidatedRecord(CreateChallengerRecordCommand command) {
        ChallengerRecord record = command.toEntity();
        getGisuUseCase.getById(command.gisuId());
        record.validateLearningSelection();
        validateRecord(record);
        return record;
    }

    private void validateRecord(ChallengerRecord record) {
        if (record.getChapterId() == null) {
            if (record.canOmitChapter()) {
                getSchoolUseCase.getSchoolDetail(record.getSchoolId());
                return;
            }
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST,
                ChallengerRecord.CHAPTER_OMISSION_RULE);
        }
        Long gisuId = record.getGisuId();
        Long schoolId = record.getSchoolId();
        Long chapterId = record.getChapterId();
        ChapterInfo chapterInfo = getChapterUseCase.byGisuAndSchool(gisuId, schoolId);

        if (!chapterInfo.id().equals(chapterId)) {
            log.debug("학교 schoolId={}가 해당 기수에 속한 지부는 {}, id={} 이지만 요청에서 제공된 chapterId={} 입니다.",
                schoolId, chapterInfo.name(), chapterInfo.id(), chapterId);

            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST,
                "주어진 학교는 해당 기수에 해당 지부에 속하지 않았습니다.");
        }
    }
}
