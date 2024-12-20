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

import java.util.List;
import java.util.ArrayList;

/**
 * This class is a resource that handles the GET requests for the validate resource.
 * 
 * This resource is used by all use cases to initialize the test.
 */
public class ValidateResource extends CoapResource {

    private SharedData sharedData;

    /**
     * Constructor for the ValidateResource class.
     * 
     * @param sharedData The shared data object.
     */

    public ValidateResource(SharedData sharedData) {
        // Set resource identifier
        super("validate");
        this.sharedData = sharedData;
        getAttributes().setTitle("Validate Resource");
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        System.out.println(ServerTimestamp.getElapsedTime()+"Received GET, Resource: validate");
        
        // Clear actuator statistics and reset global count
        this.sharedData.globalCnt = 0;
        this.sharedData.actuatorStatistics.clear();
 
        exchange.respond(ResponseCode.CONTENT, "valid");
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: validate, Payload: valid");
    }
}