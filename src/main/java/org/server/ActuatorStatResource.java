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

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;


/**
 * ActuatorStatResource is a CoAP resource that provides the statistics of the actuator.
 * 
 * This resource is used by the client to retreive the statistics of the actuator test.
 */
public class ActuatorStatResource extends CoapResource {

    private SharedData sharedData;

    /**
     * Constructor for ActuatorStatResource.
     * 
     * @param sharedData SharedData object that contains the actuator statistics.
     */
    public ActuatorStatResource(SharedData sharedData) {
        // Set resource identifier
        super("actuatorstat");
        this.sharedData = sharedData;
        // Set display name
        getAttributes().setTitle("Actuator Stat Resource");
    }

    /**
     * Handle GET requests.
     * 
     * @param exchange CoapExchange object that contains the request and response.
     */
    @Override
    public void handleGET(CoapExchange exchange) {

        // Log the request
        System.out.println(ServerTimestamp.getElapsedTime()+"Received GET, Resource: actuatorstat");

        // Get the actuator statistics
        String stats = sharedData.actuatorStatistics.getStats();

        // Send the response
        exchange.respond(ResponseCode.CONTENT, stats);
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: actuatorstat");
    }
}