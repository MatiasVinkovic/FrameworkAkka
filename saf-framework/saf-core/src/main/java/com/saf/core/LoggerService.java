package com.saf.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerService {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String level, String actorName, String action, String details) {
        String timestamp = dtf.format(LocalDateTime.now());
        // Format d'audit : [TIMESTAMP] [LEVEL] [ACTOR] ACTION -> DETAILS
        System.out.printf("[%s] [%s] [%s] %s -> %s%n",
                timestamp, level, actorName, action.toUpperCase(), details);

    }
}