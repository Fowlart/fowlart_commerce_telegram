package com.fowlart.main.az_service_bus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class ActivityTracker {

    private final ServiceBusSenderClient senderClient;

    public ActivityTracker(@Value("${servicebus.connection-string}") String connectionString,
                           @Value("${servicebus.queue.name}") String queueName) {

        this.senderClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }

    public  void sendMessage(String msg) {
        senderClient.sendMessage(new ServiceBusMessage(msg));
    }

    @PreDestroy
    public void destroy()
    {
        senderClient.close();
    }
}
