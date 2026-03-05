package com.umc.product.support.fixture;

import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class GisuFixture {

    private final ManageGisuPort manageGisuPort;

    public GisuFixture(ManageGisuPort manageGisuPort) {
        this.manageGisuPort = manageGisuPort;
    }

    public Gisu 활성_기수(Long generation) {
        return manageGisuPort.save(Gisu.create(
            generation,
            Instant.parse("2024-03-01T00:00:00Z"),
            Instant.parse("2024-08-31T23:59:59Z"),
            true
        ));
    }

    public Gisu 비활성_기수(Long generation) {
        return manageGisuPort.save(Gisu.create(
            generation,
            Instant.parse("2023-03-01T00:00:00Z"),
            Instant.parse("2023-08-31T23:59:59Z"),
            false
        ));
    }
}
