package org.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

public class ActuatorEchoResource extends CoapResource {

    private SharedData sharedData;

    public ActuatorEchoResource(SharedData sharedData) {
        // Set resource identifier
        super("actuatorecho");
        this.sharedData = sharedData;
        // Set display name
        getAttributes().setTitle("Actuator Echo Resource");
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        System.out.println("Received PUT, Payload: " + new String(exchange.getRequestPayload()));
        try {
            String payload = new String(exchange.getRequestPayload());
            JSONObject data = new JSONObject(payload);
            if (data.has("actuator-echo")) {
                int messageId = data.getInt("actuator-echo");
                this.sharedData.actuatorStatistics.received(messageId);
            } else {
                System.out.println("Invalid payload format");
            }
        } catch (Exception e) {
            System.out.println("Invalid payload format");
        }

        exchange.respond(ResponseCode.CHANGED);
    }
}