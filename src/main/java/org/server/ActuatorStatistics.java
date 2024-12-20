/********************************************************************************
 * Copyright (c) 12-20-2024 Contributors to the Eclipse Foundation
 * 
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 ********************************************************************************/

package org.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store statistics for an actuator test.
 * It keeps track of the number of sent and received messages,
 * the number of lost messages, the latency of received messages,
 * and the distribution of latencies.
 */
public class ActuatorStatistics {
    private Map<Integer, Long> pendingMessages = new ConcurrentHashMap<>();
    private Map<Integer, Integer> histogram = new HashMap<>();
    private int sentCnt;
    private int receivedCnt;
    private int timeout = 300; // in seconds
    private long latencySumMs = 0; // Sum of latencies in milliseconds

    /**
     * Clear timed out messages from the pending messages map.
     */
    public synchronized void clearTimedOutMessages() {
        long currentTime = System.currentTimeMillis() / 1000L;
        pendingMessages.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > timeout) {
                return true;
            }
            return false;
        });
    }

    /**
     * Add a sent message to the pending messages map.
     * 
     * @param messageId the message ID
     */
    public synchronized void sent(int messageId) {
        clearTimedOutMessages();
        sentCnt++;
        pendingMessages.put(messageId, System.currentTimeMillis());
    }
    
    /**
     * Add a received message to the statistics.
     * 
     * @param messageId the message ID
     */
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

    /**
     * Clear all statistics.
     */
    public synchronized void clear() {
        pendingMessages.clear();
        histogram.clear();
        sentCnt = 0;
        receivedCnt = 0;
        latencySumMs = 0;
    }

    /**
     * Get the number of sent messages.
     * 
     * @return the number of sent messages
     */
    public synchronized int averageLatency() {
        int totalMessages = histogram.values().stream().mapToInt(Integer::intValue).sum();
        if (totalMessages == 0) {
            return 0;
        }
        return (int) (latencySumMs / totalMessages); // Average latency in milliseconds
    }

    /**
     * Get the statistics as a string.
     * 
     * @return the statistics as a string
     */
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