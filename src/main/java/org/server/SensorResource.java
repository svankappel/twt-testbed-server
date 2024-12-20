package org.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class SensorResource extends CoapResource {

    private byte[] content = "Initial content".getBytes();
    private SharedData sharedData;

    public SensorResource(SharedData sharedData) {
        super("sensor");
        this.sharedData = sharedData;
        getAttributes().setTitle("Sensor Resource");
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        // GET method: returns the current content

        String payload = new String(exchange.getRequestPayload());
        System.out.println(ServerTimestamp.getElapsedTime()+"Received GET, Resource: sensor");
        
        exchange.respond(ResponseCode.CONTENT, content);
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: sensor");
    }

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
   
        exchange.respond(ResponseCode.CHANGED, ("Received: " + payload));
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: sensor");
    }
}