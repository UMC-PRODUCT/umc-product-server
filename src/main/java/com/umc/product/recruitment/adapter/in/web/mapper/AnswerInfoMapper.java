package com.umc.product.recruitment.adapter.in.web.mapper;

import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.domain.SingleAnswer;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnswerInfoMapper {

    private final GetFileUseCase getFileUseCase;

    public AnswerInfo toAnswerInfoWithPresignedUrlIfNeeded(SingleAnswer a) {
        if (a == null) {
            return null;
        }

        Map<String, Object> value = (a.getValue() == null) ? Map.of() : a.getValue();

        if (a.getAnsweredAsType() != QuestionType.PORTFOLIO) {
            return AnswerInfo.from(a);
        }

        Map<String, Object> enriched = enrichPortfolioValue(value);
        return new AnswerInfo(a.getQuestion().getId(), enriched, a.getAnsweredAsType());

    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> enrichPortfolioValue(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> out = new java.util.HashMap<>(raw);

        Object linksObj = out.get("links");
        if (linksObj instanceof List<?> linksList) {
            List<Map<String, Object>> normalizedLinks = new java.util.ArrayList<>();
            for (Object item : linksList) {
                if (item == null) {
                    continue;
                }

                if (item instanceof Map<?, ?> m) {
                    Object urlObj = m.get("url");
                    if (urlObj == null) {
                        urlObj = m.get("link");
                    }
                    if (urlObj == null) {
                        continue;
                    }

                    normalizedLinks.add(Map.of("url", String.valueOf(urlObj)));
                } else if (item instanceof String s) {
                    normalizedLinks.add(Map.of("url", s));
                }
            }
            out.put("links", normalizedLinks);
        }

        Object filesObj = out.get("files");

        if (filesObj == null && out.get("fileIds") instanceof List<?> fileIds) {
            List<Map<String, Object>> converted = new java.util.ArrayList<>();
            for (Object o : fileIds) {
                if (o == null) {
                    continue;
                }
                converted.add(Map.of("fileId", String.valueOf(o)));
            }
            out.remove("fileIds");
            out.put("files", converted);
            filesObj = out.get("files");
        }

        if (!(filesObj instanceof List<?> fileList)) {
            return out;
        }

        List<Map<String, Object>> enrichedFiles = new java.util.ArrayList<>();

        for (Object itemObj : fileList) {
            if (itemObj == null) {
                continue;
            }

            String fileId = null;

            if (itemObj instanceof Map<?, ?> m) {
                Object fid = (m.get("fileId") != null) ? m.get("fileId") : m.get("id");
                if (fid != null) {
                    fileId = String.valueOf(fid);
                }
            } else if (itemObj instanceof String s) {
                fileId = s;
            }

            if (fileId == null || fileId.isBlank()) {
                continue;
            }

            FileInfo info = getFileUseCase.getById(fileId);

            Map<String, Object> f = new java.util.HashMap<>();
            f.put("fileId", info.fileId());
            f.put("originalFileName", info.originalFileName());
            f.put("contentType", info.contentType());
            f.put("fileSize", info.fileSize());
            f.put("fileLink", info.fileLink());

            enrichedFiles.add(f);
        }
        out.put("files", enrichedFiles);
        return out;
    }
}
