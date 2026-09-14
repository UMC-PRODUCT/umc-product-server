package com.umc.product.maintenance.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class MaintenanceDomainSetConverter implements AttributeConverter<Set<MaintenanceDomain>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(Set<MaintenanceDomain> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream()
            .map(Enum::name)
            .sorted()
            .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public Set<MaintenanceDomain> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return EnumSet.noneOf(MaintenanceDomain.class);
        }
        return Arrays.stream(dbData.split(DELIMITER))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(MaintenanceDomain::valueOf)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(MaintenanceDomain.class)));
    }
}
