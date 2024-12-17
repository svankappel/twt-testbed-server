package org.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class HelloResource extends CoapResource {

    public HelloResource() {
        // Set resource identifier
        super("hello");
        // Set display name
        getAttributes().setTitle("Hello Resource");
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        // Respond to the request
        exchange.respond(ResponseCode.CONTENT, "Hello, CoAP server!");
    }
}