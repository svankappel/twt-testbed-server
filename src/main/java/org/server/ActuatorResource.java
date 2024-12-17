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

public class ActuatorResource extends CoapResource {

    private SharedData sharedData;
    private int delay = 5;
    private int x = 5;
    private int y = 10;
    private Random random = new Random();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ActuatorResource(SharedData sharedData) {
        super("actuator");
        this.sharedData = sharedData;
        getAttributes().setTitle("Actuator Resource");
        setObservable(true); // Enable observing
        setObserveType(Type.CON); // Configure the notification type to CONs
        getAttributes().setObservable(); // Mark observable in the Link-Format
        startNotifier();
    }

    private void startNotifier() {
        scheduler.schedule(new UpdateTask(), delay, TimeUnit.SECONDS);
    }

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

    @Override
    public void handleGET(CoapExchange exchange) {
        if (exchange.getRequestPayload() != null && exchange.getRequestPayload().length > 0) {
            System.out.println("Received GET, Resource: actuator, Payload: " + new String(exchange.getRequestPayload()));
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

        String message = String.format("{\"actuator-value\":%d}", sharedData.globalCnt);
        Response response = new Response(ResponseCode.CONTENT);
        response.setPayload(message);
        response.setType(Type.NON); // Set the response type to NON
        exchange.respond(response);
        if (!(exchange.getRequestOptions().getObserve()==1)) {
            this.sharedData.actuatorStatistics.sent(sharedData.globalCnt);
            sharedData.globalCnt++;
        }
    }

    private void notifyObservers() {
        changed(); // Notify all observers
    }
}