package com.umc.product.community.adapter.in.web;

import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.community.domain.CommunityThreadLinks;
import com.umc.product.global.web.AppLinkPathProvider;

/** 커뮤니티 스레드 공유 링크 경로를 apple-app-site-association에 등록한다(#1268). */
@Component
public class CommunityThreadAppLinkPathProvider implements AppLinkPathProvider {

    @Override
    public List<String> appLinkPaths() {
        return List.of(CommunityThreadLinks.APP_LINK_PATH_PATTERN);
    }
}
