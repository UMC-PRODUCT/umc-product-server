package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.post.ToggleScrapUseCase;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.application.port.out.LoadScrapPort;
import com.umc.product.community.application.port.out.SaveScrapPort;
import com.umc.product.community.domain.Scrap;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScrapCommandService implements ToggleScrapUseCase {

    private final LoadPostPort loadPostPort;
    private final LoadScrapPort loadScrapPort;
    private final SaveScrapPort saveScrapPort;

    @Override
    public ScrapResult toggle(Long postId, Long challengerId) {
        // 게시글 존재 확인
        loadPostPort.findById(postId)
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        // 스크랩 토글
        boolean scrapped;
        if (loadScrapPort.existsByPostIdAndChallengerId(postId, challengerId)) {
            // 이미 스크랩했으면 취소
            saveScrapPort.deleteByPostIdAndChallengerId(postId, challengerId);
            scrapped = false;
        } else {
            // 스크랩 추가
            Scrap scrap = Scrap.create(postId, challengerId);
            saveScrapPort.save(scrap);
            scrapped = true;
        }

        // 현재 스크랩 수 조회
        int scrapCount = loadScrapPort.countByPostId(postId);

        return new ScrapResult(scrapped, scrapCount);
    }
}
