package org.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.List;
import java.util.ArrayList;

public class ValidateResource extends CoapResource {

    private SharedData sharedData;

    private ActuatorResource actuatorResource;

    public ValidateResource(SharedData sharedData, ActuatorResource actuatorResource) {
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