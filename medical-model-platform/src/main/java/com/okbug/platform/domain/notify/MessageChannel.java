package com.okbug.platform.domain.notify;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum MessageChannel {
    INBOX("inbox"),
    EMAIL("email"),
    SMS("sms");

    private final String code;

    MessageChannel(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Set<String> allCodes() {
        return Arrays.stream(values()).map(MessageChannel::code).collect(Collectors.toSet());
    }

    public static String allCsv() {
        return String.join(",", allCodes());
    }

    public static MessageChannel fromCode(String code) {
        if (code == null) return null;
        String c = code.trim().toLowerCase();
        for (MessageChannel ch : values()) {
            if (ch.code.equals(c)) return ch;
        }
        return null;
    }
}


