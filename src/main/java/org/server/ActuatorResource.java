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
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * ActuatorResource is a CoAP resource that simulates an actuator.
 * 
 * The client can start and stop observing the actuator resource.
 * When observing, the actuator resource sends a notification with
 * a random interval. The min and max interval is sent with the
 * first GET request that starts the observe.
 */

public class ActuatorResource extends CoapResource {

    private SharedData sharedData;
    private int delay = 5;
    private int x = 5;
    private int y = 10;
    private Random random = new Random();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private int localCnt = 0;

    private CoapExchange exchange;
    private boolean isObserve = false;


    /**
     * Constructor for creating an actuator resource.
     * 
     * @param sharedData the shared data
     */
    public ActuatorResource(SharedData sharedData) {
        super("actuator");
        this.sharedData = sharedData;
        getAttributes().setTitle("Actuator Resource");
        setObservable(true); // Enable observing
        setObserveType(Type.CON); // Configure the notification type to CONs
        getAttributes().setObservable(); // Mark observable in the Link-Format
        startNotifier();
    }

    /**
     * Start the notifier that sends the observe notifications.
     */
    private void startNotifier() {
        scheduler.schedule(new UpdateTask(), delay, TimeUnit.SECONDS);
    }

    /**
     * The UpdateTask is a task that sends the observe notifications.
     */
    private class UpdateTask implements Runnable {
        @Override
        public void run() {
            notifyObservers();
            reschedule();
        }

        private void reschedule() {
            delay = random.nextInt(y - x + 1) + x;
            scheduler.schedule(new UpdateTask(), delay, TimeUnit.SECONDS);
        }
    }


    /**
     * Handle the GET request.
     * 
     * The actuator resource can be observed. The client can start and stop
     * observing the actuator resource. When observing, the actuator resource
     * sends a notification with a random interval. The min and max interval
     * is sent with the first GET request that starts the observe.
     * 
     * @param exchange the exchange
     */
    @Override
    public void handleGET(CoapExchange exchange) {
        this.exchange = exchange;

        // Start observing
        if(exchange.getRequestOptions().hasObserve() && exchange.getRequestOptions().getObserve() == 0) {
            System.out.println(ServerTimestamp.getElapsedTime() + 
                               "Received GET, " +
                               "Resource: actuator, " +
                               "Option: start observe, " +
                               "Payload: " + new String(exchange.getRequestPayload()));
            isObserve = true;
            localCnt = 0;
            sharedData.globalCnt = 0;

            String message = String.format("observe started");
            Response response = new Response(ResponseCode.CONTENT);
            response.setPayload(message);
            response.setType(Type.NON); // Set the response type to NON
            exchange.respond(response);
            System.out.println(ServerTimestamp.getElapsedTime() + 
                               "Sent response, " +
                               "Resource: actuator, " +
                               "Payload: " + message);
        }

        // Stop observing
        if(exchange.getRequestOptions().hasObserve() && exchange.getRequestOptions().getObserve() == 1) {
            System.out.println(ServerTimestamp.getElapsedTime() + 
                               "Received GET, " +
                               "Resource: actuator, " +
                               "Option: stop observe, " +
                               "Payload: " + new String(exchange.getRequestPayload()));
            isObserve = false;
            String message = String.format("observe stopped");
            Response response = new Response(ResponseCode.CONTENT);
            response.setPayload(message);
            response.setType(Type.NON); // Set the response type to NON
            exchange.respond(response);
            System.out.println(ServerTimestamp.getElapsedTime() + 
                               "Sent response, " +
                               "Resource: actuator, " +
                               "Payload: " + message);
        }

        // Get the min and max interval
        if (exchange.getRequestPayload() != null && exchange.getRequestPayload().length > 0) {

            try {
                String[] parts = new String(exchange.getRequestPayload()).split("/");
                if (parts.length >= 3) {
                    x = Integer.parseInt(parts[1]);
                    y = Integer.parseInt(parts[2]);
                } else {
                    System.out.println("Invalid payload format");
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                System.out.println("Invalid payload format");
            }
        }
    }

    /**
     * Clear the observers.
     * 
     * This stops the observe notifications.
     */
    public void clearObservers() {
        isObserve = false;
        if(exchange.getRequestOptions().hasObserve()){
            exchange.getRequestOptions().removeObserve();
        }
    }

    /**
     * Notify the observers.
     * 
     * This is called to notify the observers. It sends the observe
     * notification with the actuator value.
     */
    private void notifyObservers() {
        
        //detect if the client disconnected without stopping the observe
        if(sharedData.globalCnt == 0 && localCnt != 0){ //if the client disconnected without stopping the observe
            isObserve = false;                          //global cnt will be updated at reconnection               
            localCnt = 0;
            sharedData.globalCnt = 0;
        }

        // Send the observe notification
        if(isObserve) {
            String message = String.format("{\"actuator-value\":%d}", sharedData.globalCnt);
            Response response = new Response(ResponseCode.CONTENT);
            response.setPayload(message);
            response.setType(Type.NON); // Set the response type to NON
            exchange.respond(response);
            this.sharedData.actuatorStatistics.sent(sharedData.globalCnt);
            sharedData.globalCnt++;
            localCnt++;
            System.out.println(ServerTimestamp.getElapsedTime() + 
                               "Sent Observe notification, " +
                               "Resource: actuator, " +
                               "Payload: " + message);
        }
    }
}