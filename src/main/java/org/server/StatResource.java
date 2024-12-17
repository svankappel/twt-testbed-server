package org.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;


public class StatResource extends CoapResource {

    private SharedData sharedData;

    public StatResource(SharedData sharedData) {
        // Set resource identifier
        super("stat");
        this.sharedData = sharedData;
        getAttributes().setTitle("Stat Resource");
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        System.out.println("Received GET, Resource: stat");

        // Respond to the request
        byte[] content = (Integer.toString(this.sharedData.globalCnt)).getBytes();
        exchange.respond(ResponseCode.CONTENT, content);
    }
}