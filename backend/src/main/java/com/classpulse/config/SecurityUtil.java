package com.classpulse.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        Object principal = authentication.getPrincipal();

        if (principal instanceof Long id) {
            return id;
        }
        if (principal instanceof Number num) {
            return num.longValue();
        }
        if (principal instanceof String str) {
            return Long.parseLong(str);
        }
        throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
    }
}
