package org.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Random;

public class LargeDownloadResource extends CoapResource {

    private SharedData sharedData;
    private Random random = new Random();

    public LargeDownloadResource(SharedData sharedData) {
        super("largedownload");
        this.sharedData = sharedData;
        getAttributes().setTitle("Large Download Resource");
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        System.out.println(ServerTimestamp.getElapsedTime()+"Received PUT, Resource: largedownload, Payload: " + new String(exchange.getRequestPayload()));
        this.sharedData.globalCnt++;

        String receivedData = new String(exchange.getRequestPayload());
        String[] parts = receivedData.split("/");
        if (parts.length < 3) {
            exchange.respond(ResponseCode.BAD_REQUEST, "Invalid format");
            return;
        }

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

            exchange.respond(ResponseCode.CHANGED, response.getBytes());
            System.out.println(ServerTimestamp.getElapsedTime()+"Sent Response, Resource: largedownload");
        } catch (NumberFormatException e) {
            exchange.respond(ResponseCode.BAD_REQUEST, "Invalid format");
            System.out.println(ServerTimestamp.getElapsedTime()+"Sent Error");
        }
    }
}