package com.umc.product.global.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ClientDeviceType User-Agent 판정")
class ClientDeviceTypeTest {

    @Test
    @DisplayName("iPhone/iPad는 IOS, Android는 ANDROID, 그 밖의 User-Agent는 DESKTOP이다")
    void fromUserAgent_classifiesDevice() {
        assertThat(ClientDeviceType.fromUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X)"))
            .isEqualTo(ClientDeviceType.IOS);
        assertThat(ClientDeviceType.fromUserAgent("Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X)"))
            .isEqualTo(ClientDeviceType.IOS);
        assertThat(ClientDeviceType.fromUserAgent("Mozilla/5.0 (Linux; Android 14)"))
            .isEqualTo(ClientDeviceType.ANDROID);
        assertThat(ClientDeviceType.fromUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"))
            .isEqualTo(ClientDeviceType.DESKTOP);
    }

    @Test
    @DisplayName("User-Agent가 없거나 비어 있으면 UNKNOWN이다")
    void fromUserAgent_missing() {
        assertThat(ClientDeviceType.fromUserAgent(null)).isEqualTo(ClientDeviceType.UNKNOWN);
        assertThat(ClientDeviceType.fromUserAgent(" ")).isEqualTo(ClientDeviceType.UNKNOWN);
    }
}
