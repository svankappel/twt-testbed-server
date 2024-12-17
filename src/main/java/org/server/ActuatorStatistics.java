package org.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActuatorStatistics {
    private Map<Integer, Long> pendingMessages = new ConcurrentHashMap<>();
    private Map<Integer, Integer> histogram = new HashMap<>();
    private int sentCnt;
    private int receivedCnt;
    private int timeout = 300; // in seconds
    private long latencySumMs = 0; // Sum of latencies in milliseconds

    public synchronized void clearTimedOutMessages() {
        long currentTime = System.currentTimeMillis() / 1000L;
        pendingMessages.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > timeout) {
                return true;
            }
            return false;
        });
    }

    public synchronized void sent(int messageId) {
        clearTimedOutMessages();
        sentCnt++;
        pendingMessages.put(messageId, System.currentTimeMillis());
    }

    public synchronized void received(int messageId) {
        clearTimedOutMessages();
        if (pendingMessages.containsKey(messageId)) {
            long sentTime = pendingMessages.remove(messageId);
            long latencyMs = System.currentTimeMillis() - sentTime;
            int latencySeconds = (int) (latencyMs / 1000);
            histogram.put(latencySeconds, histogram.getOrDefault(latencySeconds, 0) + 1);
            latencySumMs += latencyMs;
            receivedCnt++;
        }
    }

    public synchronized void clear() {
        pendingMessages.clear();
        histogram.clear();
        sentCnt = 0;
        receivedCnt = 0;
        latencySumMs = 0;
    }

    public synchronized int averageLatency() {
        int totalMessages = histogram.values().stream().mapToInt(Integer::intValue).sum();
        if (totalMessages == 0) {
            return 0;
        }
        return (int) (latencySumMs / totalMessages); // Average latency in milliseconds
    }

    public synchronized String getStats() {
        StringBuilder stats = new StringBuilder();
        histogram.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> stats.append(entry.getKey()).append(";").append(entry.getValue()).append("\n"));
        stats.append("lost;").append(sentCnt-receivedCnt).append("\n");
        stats.append("average_ms;").append(averageLatency());
        return stats.toString();
    }
}