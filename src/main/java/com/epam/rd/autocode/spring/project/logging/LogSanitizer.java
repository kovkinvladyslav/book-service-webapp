package com.epam.rd.autocode.spring.project.logging;

public final class LogSanitizer {
    private LogSanitizer(){}

    public static String mask(Object value) {
        if (value == null) return "null";
        String s = value.toString();
        if (s.length() > 400) s = s.substring(0, 400) + "…";
        return s;
    }

    public static String maskEmail(String email) {
        if (email == null) return "null";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }

    public static String maskPassword(Object pwd) { return "***"; }

    public static String brief(Object dto) {
        if (dto == null) return "null";
        String s = dto.toString();
        s = s.replaceAll("password=([^,}\\]]+)", "password=***");
        if (s.length() > 500) s = s.substring(0, 500) + "…";
        return s;
    }
}
