package org.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ActuatorStatResource extends CoapResource {

    private SharedData sharedData;

    public ActuatorStatResource(SharedData sharedData) {
        // Set resource identifier
        super("actuatorstat");
        this.sharedData = sharedData;
        // Set display name
        getAttributes().setTitle("Actuator Stat Resource");
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        System.out.println("Received GET, Resource: actuatorstat");
        String stats = sharedData.actuatorStatistics.getStats();
        exchange.respond(ResponseCode.CONTENT, stats);
    }
}