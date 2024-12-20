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
 * This class is a resource that handles the GET requests for the stat resource.
 * 
 * This resource is used by all uses cases to retrieve the global counter.
 */
public class StatResource extends CoapResource {

    private SharedData sharedData;


    /**
     * Constructor for the StatResource class.
     * 
     * @param sharedData The shared data object.
     */
    public StatResource(SharedData sharedData) {
        // Set resource identifier
        super("stat");
        this.sharedData = sharedData;
        getAttributes().setTitle("Stat Resource");
    }

    /**
     * This method handles the GET request for the stat resource.
     * 
     * @param exchange The exchange containing the request.
     */
    @Override   
    public void handleGET(CoapExchange exchange) {

        // Log the request
        System.out.println(ServerTimestamp.getElapsedTime()+"Received GET, Resource: stat");

        // Respond to the request
        byte[] content = (Integer.toString(this.sharedData.globalCnt)).getBytes();
        exchange.respond(ResponseCode.CONTENT, content);
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: stat");
    }
}