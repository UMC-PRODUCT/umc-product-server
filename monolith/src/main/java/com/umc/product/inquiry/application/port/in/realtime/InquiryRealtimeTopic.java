package com.umc.product.inquiry.application.port.in.realtime;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InquiryRealtimeTopic {

    private static final String PREFIX = "/topic/inquiries/";
    private static final Pattern DESTINATION = Pattern.compile("^/topic/inquiries/([1-9][0-9]{0,18})$");

    private InquiryRealtimeTopic() {
    }

    public static String of(Long inquiryId) {
        return PREFIX + inquiryId;
    }

    public static boolean matches(String destination) {
        return destination != null && destination.startsWith(PREFIX);
    }

    public static Optional<Long> parseInquiryId(String destination) {
        if (destination == null) {
            return Optional.empty();
        }
        Matcher matcher = DESTINATION.matcher(destination);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(matcher.group(1)));
        } catch (NumberFormatException overflow) {
            return Optional.empty();
        }
    }
}
