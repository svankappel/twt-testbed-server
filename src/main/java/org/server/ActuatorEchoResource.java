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
import org.json.JSONObject;

/**
 * ActuatorEchoResource is a CoAP resource that handles PUT requests.
 * 
 * This resource is used by the actuator usecase.
 * The client echoes the actuator message on this resource. It is
 * used to measure the round trip time of the actuator message
 */
public class ActuatorEchoResource extends CoapResource {

    private SharedData sharedData;

    /**
     * Constructor for ActuatorEchoResource.
     * 
     * @param sharedData SharedData object that contains shared data between resources
     */
    public ActuatorEchoResource(SharedData sharedData) {

        // Set resource identifier
        super("actuatorecho");
        this.sharedData = sharedData;

        // Set display name
        getAttributes().setTitle("Actuator Echo Resource");
    }

    /**
     * Handles PUT requests.
     * 
     * @param exchange CoapExchange object that contains the request
     */
    @Override
    public void handlePUT(CoapExchange exchange) {

        // Print received message
        System.out.println(ServerTimestamp.getElapsedTime()+"Received PUT, Resource: actuatorecho, Payload: " + new String(exchange.getRequestPayload()));
        
        // Parse payload
        try {
            String payload = new String(exchange.getRequestPayload());
            JSONObject data = new JSONObject(payload);
            if (data.has("actuator-echo")) {
                
                // Update actuator statistics
                int messageId = data.getInt("actuator-echo");
                this.sharedData.actuatorStatistics.received(messageId);
            } else {
                System.out.println("Invalid payload format");
            }
        } catch (Exception e) {
            System.out.println("Invalid payload format");
        }

        //noresponse
        //exchange.respond(ResponseCode.CHANGED);
    }
}