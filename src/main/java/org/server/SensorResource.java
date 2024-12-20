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
 * This class is a resource that handles the GET and PUT requests for the sensor
 * resource.
 * 
 * This resource is used by the sensor and multi packet use cases.
 */
public class SensorResource extends CoapResource {

    private byte[] content = "Initial content".getBytes();
    private SharedData sharedData;

    public SensorResource(SharedData sharedData) {
        super("sensor");
        this.sharedData = sharedData;
        getAttributes().setTitle("Sensor Resource");
    }

    /**
     * This method handles the GET request for the sensor resource.
     * 
     * @param exchange The exchange containing the request.
     */
    @Override
    public void handleGET(CoapExchange exchange) {
        // GET method: returns the current content
        String payload = new String(exchange.getRequestPayload());
        System.out.println(ServerTimestamp.getElapsedTime()+"Received GET, Resource: sensor");
        
        exchange.respond(ResponseCode.CONTENT, content);
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: sensor");
    }

    /**
     * This method handles the PUT request for the sensor resource.
     * 
     * @param exchange The exchange containing the request.
     */
    @Override
    public void handlePUT(CoapExchange exchange) {
        // PUT method: updates the content with the received payload
        String payload = new String(exchange.getRequestPayload());
        if (!payload.isEmpty()) {
            System.out.println(ServerTimestamp.getElapsedTime()+"Received PUT, Resource: sensor, Payload: " + payload);
        } else {
            System.out.println(ServerTimestamp.getElapsedTime()+"Received PUT, Resource: sensor");
        }
        this.sharedData.globalCnt++;

        //wait for 20 - 50 ms to simulate prossesing and latency
        try {
            Thread.sleep((long) (Math.random() * 30 + 20));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // respond to the request
        exchange.respond(ResponseCode.CHANGED, ("Received: " + payload));
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: sensor");
    }
}