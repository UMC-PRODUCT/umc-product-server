package com.umc.product.global.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class CustomHighlightConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        switch (level.toInt()) {
            case Level.ERROR_INT:
                return ANSIConstants.RED_FG;  // 빨강
            case Level.WARN_INT:
                return ANSIConstants.YELLOW_FG;  // 노랑
            case Level.INFO_INT:
                return ANSIConstants.BLUE_FG; // 파랑
            case Level.DEBUG_INT:
                return ANSIConstants.CYAN_FG;  // 청록
            case Level.TRACE_INT:
                return ANSIConstants.MAGENTA_FG; // 마젠타
            default:
                return ANSIConstants.DEFAULT_FG;
        }
    }
}
