package org.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class LargeUploadAckResource extends CoapResource {

    private byte[] content = "Initial content".getBytes();
    private SharedData sharedData;

    public LargeUploadAckResource(SharedData sharedData) {
        super("largeuploadack");
        this.sharedData = sharedData;
        getAttributes().setTitle("Large Upload Ack Resource");
    }


    @Override
    public void handlePUT(CoapExchange exchange) {

        System.out.println(ServerTimestamp.getElapsedTime()+"Received PUT, Resource: largeuploadack, Payload: " + new String(exchange.getRequestPayload(), 0, Math.min(20, exchange.getRequestPayload().length)) + (exchange.getRequestPayload().length > 20 ? "..." : ""));
        
        this.sharedData.globalCnt++;
        content = exchange.getRequestPayload();

        //wait for 20 - 50 ms to simulate prossesing and latency
        try {
            Thread.sleep((long) (Math.random() * 30 + 20));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        exchange.respond(ResponseCode.CHANGED, ("Received: " + new String(content, 0, Math.min(8, content.length))).getBytes());
        System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: largeuploadack");
    }
}