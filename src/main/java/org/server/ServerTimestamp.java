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

import java.time.Duration;
import java.time.Instant;

/**
 * This class provides a method to get the elapsed time since the server started.
 */
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