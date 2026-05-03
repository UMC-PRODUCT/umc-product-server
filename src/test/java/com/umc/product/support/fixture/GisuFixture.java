package com.umc.product.support.fixture;

import com.umc.product.organization.application.port.out.command.SaveGisuPort;
import com.umc.product.organization.domain.Gisu;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class GisuFixture extends FixtureSupport {

    private final SaveGisuPort saveGisuPort;

    private final Instant START_AT = Instant.parse("2024-03-01T00:00:00Z");
    private final Instant END_AT = Instant.parse("2024-08-31T23:59:59Z");

    public GisuFixture(SaveGisuPort saveGisuPort) {
        this.saveGisuPort = saveGisuPort;
    }

    public Gisu 활성_기수(Long generation) {
        return saveGisuPort.save(Gisu.create(
            generation,
            START_AT,
            END_AT,
            true
        ));
    }

    public Gisu 비활성_기수(Long generation) {
        return saveGisuPort.save(Gisu.create(
            generation,
            START_AT,
            END_AT,
            false
        ));
    }

    public Gisu 활성_기수() {
        return 활성_기수(Math.abs(com.umc.product.support.CommonFixture.MONKEY.giveMeOne(Long.class)));
    }

    public Gisu 비활성_기수() {
        return 비활성_기수(Math.abs(com.umc.product.support.CommonFixture.MONKEY.giveMeOne(Long.class)));
    }
}
