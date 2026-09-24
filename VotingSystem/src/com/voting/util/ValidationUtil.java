package com.voting.util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern VOTER_CODE_PATTERN =
            Pattern.compile("^[A-Za-z0-9]{3,20}$");

    private ValidationUtil() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidVoterCode(String code) {
        return code != null && VOTER_CODE_PATTERN.matcher(code.trim()).matches();
    }
}
