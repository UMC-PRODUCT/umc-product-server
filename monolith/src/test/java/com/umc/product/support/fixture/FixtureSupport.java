package com.umc.product.support.fixture;

import static com.umc.product.support.CommonFixture.MONKEY;

abstract class FixtureSupport {

    /**
     * prefix를 붙인 무작위 문자열을 maxLength에 맞게 생성합니다.
     */
    protected String fixtureString(String prefix, int maxLength) {
        String sampled = MONKEY.giveMeOne(String.class);
        String value = sampled == null || sampled.isBlank()
            ? prefix
            : prefix + sampled;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * value를 제공하면 해당 값을 제공합니다.
     */
    protected String valueOrFixture(String value, String prefix, int maxLength) {
        if (value == null || value.isBlank()) {
            return fixtureString(prefix, maxLength);
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    protected String fixtureUrl(String path) {
        return "https://test.umc.product/" + path + "/" + fixtureString("link", 16);
    }
}
