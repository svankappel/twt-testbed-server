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

    private int localCnt = 0;

    private CoapExchange exchange;
    private boolean isObserve = false;

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
        this.exchange = exchange;

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

    public void clearObservers() {
        isObserve = false;
        if(exchange.getRequestOptions().hasObserve()){
            exchange.getRequestOptions().removeObserve();
        }
    }

    private void notifyObservers() {
        
        if(sharedData.globalCnt == 0 && localCnt != 0){ //if the client disconnected without stopping the observe
            isObserve = false;                          //global cnt will be updated at reconnection               
            localCnt = 0;
            sharedData.globalCnt = 0;
        }

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