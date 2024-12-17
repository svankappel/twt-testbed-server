package org.server;

import java.time.Duration;
import java.time.Instant;

public class ServerTimestamp {

    private static final Instant startTime = Instant.now();

    public static String getElapsedTime() {
        Duration elapsedTime = Duration.between(startTime, Instant.now());
        long hours = elapsedTime.toHours();
        long minutes = elapsedTime.toMinutes() % 60;
        long seconds = elapsedTime.getSeconds() % 60;
        long millis = elapsedTime.toMillis() % 1000;
        return String.format("[%02d:%02d:%02d.%03d] ", hours, minutes, seconds, millis);
    }
}