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


/**
 * This class provides a shared data object that can be accessed by multiple resources.
 */
public class SharedData {
    public int globalCnt = 0;
    public ActuatorStatistics actuatorStatistics = new ActuatorStatistics();
}