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

import java.util.Random;

/**
 * LargeDownloadResource is a CoAP resource that responds with a large payload
 * 
 * This resource is used by the Large Packet use case. The client sends a PUT request
 * specifying the size of the response payload.
 */
public class LargeDownloadResource extends CoapResource {

    private SharedData sharedData;
    private Random random = new Random();

    /**
     * Constructor for LargeDownloadResource.
     * 
     * @param sharedData SharedData object that contains the global counter.
     */

    public LargeDownloadResource(SharedData sharedData) {
        super("largedownload");
        this.sharedData = sharedData;
        getAttributes().setTitle("Large Download Resource");
    }

    /**
     * Handle PUT requests.
     * 
     * @param exchange CoapExchange object that contains the request and response.
     */
    @Override
    public void handlePUT(CoapExchange exchange) {

        // Log the request
        System.out.println(ServerTimestamp.getElapsedTime()+"Received PUT, Resource: largedownload, Payload: " + new String(exchange.getRequestPayload()));
        this.sharedData.globalCnt++;

        // Get the payload
        String receivedData = new String(exchange.getRequestPayload());
        String[] parts = receivedData.split("/");
        if (parts.length < 3) {
            exchange.respond(ResponseCode.BAD_REQUEST, "Invalid format");
            return;
        }

        // Generate the response
        try {
            String number = parts[1];
            int size = Integer.parseInt(parts[2]);

            String header = "/" + number + "/\n";
            String footer = "/largedownload/";
            int contentSize = size - header.length() - footer.length() - 1; // -1 for the newline before footer
            StringBuilder randomChars = new StringBuilder(contentSize);
            for (int i = 0; i < contentSize; i++) {
                randomChars.append((char) ('a' + random.nextInt(26)));
            }
            String formattedContent = randomChars.toString().replaceAll("(.{80})", "$1\n");
            String response = header + formattedContent + "\n" + footer;

            //wait for 20 - 50 ms to simulate prossesing and latency
            try {
                Thread.sleep((long) (Math.random() * 30 + 20));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Send the response
            exchange.respond(ResponseCode.CHANGED, response.getBytes());
            System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: largedownload");
        } catch (NumberFormatException e) {
            exchange.respond(ResponseCode.BAD_REQUEST, "Invalid format");
            System.out.println(ServerTimestamp.getElapsedTime()+"Sent Error");
        }
    }
}