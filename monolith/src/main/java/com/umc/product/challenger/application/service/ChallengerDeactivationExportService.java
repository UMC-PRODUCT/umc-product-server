package com.umc.product.challenger.application.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.query.ExportChallengerDeactivationUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ExportChallengerDeactivationQuery;
import com.umc.product.challenger.application.port.in.query.dto.ExportChallengerDeactivationQuery.Column;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;
import com.umc.product.challenger.application.port.out.SearchChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationExportRow;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerDeactivationExportService implements ExportChallengerDeactivationUseCase {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final int MAX_EXPORT_ROWS = 50_000;

    private final ChallengerDeactivationQueryService deactivationQueryService;
    private final SearchChallengerDeactivationPort searchChallengerDeactivationPort;
    private final GetGisuUseCase getGisuUseCase;

    @Override
    public byte[] exportCsv(Long memberId, ExportChallengerDeactivationQuery query) {
        SearchChallengerDeactivationQuery scope = deactivationQueryService.resolveSearchScope(memberId, query.scope());
        validate(query);

        Instant startInclusive = query.startDate() == null ? null : startOfDay(query.startDate());
        Instant endExclusive = query.endDate() == null ? null : startOfDay(query.endDate().plusDays(1));
        List<ChallengerDeactivationExportRow> rows = searchChallengerDeactivationPort.listExportRows(
            scope, query, startInclusive, endExclusive, MAX_EXPORT_ROWS + 1
        );
        if (rows.size() > MAX_EXPORT_ROWS) {
            throw badRequest("한 번에 5만 건까지 내보낼 수 있어요. 조회 기간이나 조건을 줄여주세요.");
        }

        Long generation = query.columns().contains(Column.ORGANIZATION) && !rows.isEmpty()
            ? getGisuUseCase.getByIds(Set.of(scope.gisuId())).stream()
                .map(GisuInfo::generation)
                .findFirst()
                .orElse(null)
            : null;
        List<Column> columns = Arrays.stream(Column.values()).filter(query.columns()::contains).toList();
        StringBuilder csv = new StringBuilder("\uFEFF");
        appendLine(csv, columns.stream().flatMap(column -> headers(column).stream()).toList());
        for (ChallengerDeactivationExportRow row : rows) {
            appendLine(csv, cells(columns, row, generation));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void validate(ExportChallengerDeactivationQuery query) {
        if (query.deactivationIds().size() > ExportChallengerDeactivationQuery.MAX_SELECTED_IDS) {
            throw badRequest("한 번에 최대 1,000건까지 선택할 수 있어요.");
        }
        if (query.columns().isEmpty()) {
            throw badRequest("내보낼 열을 하나 이상 선택해주세요.");
        }

        LocalDate startDate = query.startDate();
        LocalDate endDate = query.endDate();
        if (startDate == null && endDate == null && !query.deactivationIds().isEmpty()) {
            return;
        }
        if (startDate == null || endDate == null) {
            throw badRequest("내보낼 기간의 시작일과 종료일을 모두 입력해주세요.");
        }
        if (startDate.isAfter(endDate) || startDate.getYear() < 1 || endDate.getYear() > 9_999) {
            throw badRequest("내보낼 기간을 확인해주세요.");
        }
    }

    private Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(KOREA_ZONE).toInstant();
    }

    private List<String> headers(Column column) {
        return switch (column) {
            case ORGANIZATION -> List.of("기수", "학교", "파트");
            case REASON -> List.of("탈부 유형", "탈부 사유", "기타 사유");
            case REGISTERED_DATE -> List.of("탈부 날짜");
            case REGISTRAR_ROLE -> List.of("등록자 역할");
            case MEMO -> List.of("메모");
            case NAME -> List.of("이름", "닉네임");
        };
    }

    private List<Object> cells(List<Column> columns, ChallengerDeactivationExportRow row, Long generation) {
        List<Object> values = new ArrayList<>();
        for (Column column : columns) {
            switch (column) {
                case ORGANIZATION -> {
                    values.add(generation);
                    values.add(row.schoolName());
                    values.add(row.part() == null ? null : row.part().getDisplayName());
                }
                case REASON -> {
                    values.add(row.status() == ChallengerStatus.WITHDRAWN ? "자진 탈부" : "타의 탈부");
                    values.add(row.reason().getDisplayName());
                    values.add(row.reasonDetail());
                }
                case REGISTERED_DATE -> values.add(row.registeredAt().atZone(KOREA_ZONE).toLocalDate());
                // 당시 등록자 역할은 저장하지 않는다.
                case REGISTRAR_ROLE -> values.add(null);
                case MEMO -> values.add(row.memo());
                case NAME -> {
                    values.add(row.name());
                    values.add(row.nickname());
                }
            }
        }
        return values;
    }

    private void appendLine(StringBuilder csv, List<?> values) {
        csv.append(values.stream().map(this::encodeCell).collect(Collectors.joining(","))).append("\r\n");
    }

    private String encodeCell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        String stripped = text.stripLeading();
        // 스프레드시트의 수식으로 평가될 수 있는 문자열은 텍스트로 기록한다.
        if (!text.isEmpty() && ("\t\r\n".indexOf(text.charAt(0)) >= 0
            || (!stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0)) >= 0))) {
            text = "'" + text;
        }
        if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0 || text.indexOf('\r') >= 0 || text.indexOf('\n') >= 0) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private CommonException badRequest(String message) {
        return new CommonException(CommonErrorCode.BAD_REQUEST, message);
    }
}
