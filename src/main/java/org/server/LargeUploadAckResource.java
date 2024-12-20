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
 * LargeUploadAckResource is a CoAP resource that responds with an acknowledgment
 * 
 * This resource is used by the Large Packet use case. The client sends a PUT request
 * with a large payload. The server responds with an acknowledgment.
 */

public class LargeUploadAckResource extends CoapResource {

    private byte[] content = "Initial content".getBytes();
    private SharedData sharedData;

    /**
     * Constructor for LargeUploadAckResource.
     * 
     * @param sharedData SharedData object that contains the global counter.
     */
    public LargeUploadAckResource(SharedData sharedData) {
        super("largeuploadack");
        this.sharedData = sharedData;
        getAttributes().setTitle("Large Upload Ack Resource");
    }

    /**
     * Handle PUT requests.
     * 
     * @param exchange CoapExchange object that contains the request and response.
     */
    @Override
    public void handlePUT(CoapExchange exchange) {

        // Log the request
        System.out.println(ServerTimestamp.getElapsedTime()+"Received PUT, Resource: largeuploadack, Payload: " + new String(exchange.getRequestPayload(), 0, Math.min(20, exchange.getRequestPayload().length)) + (exchange.getRequestPayload().length > 20 ? "..." : ""));
        
        // Increment the global counter
        this.sharedData.globalCnt++;

        // Get the payload
        content = exchange.getRequestPayload();

        //wait for 20 - 50 ms to simulate prossesing and latency
        try {
            Thread.sleep((long) (Math.random() * 30 + 20));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Send the response
        exchange.respond(ResponseCode.CHANGED, ("Received: " + new String(content, 0, Math.min(8, content.length))).getBytes());
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: largeuploadack");
    }
}