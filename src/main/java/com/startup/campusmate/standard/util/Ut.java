package com.startup.campusmate.standard.util;

import lombok.SneakyThrows;

public class Ut {
    public static class str {
        public static boolean isBlank(String str) {
            return str == null || str.trim().length() == 0;
        }

        public static boolean hasLength(String str) {
            return !isBlank(str);
        }
    }

    public static class thread {
        @SneakyThrows
        public static void sleep(long millis) {
            Thread.sleep(millis);
        }
    }
}