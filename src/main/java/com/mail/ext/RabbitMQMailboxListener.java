package com.mail.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class RabbitMQMailboxListener {
    private final ConnectionFactory factory;
    private final MailboxActionHandler actionHandler;

    public RabbitMQMailboxListener(String host, MailboxActionHandler handler) {
        this.factory = new ConnectionFactory();
        factory.setHost(host);
        this.actionHandler = handler;
    }

    public void startListening(String queueName) throws Exception {
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, true, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String json = new String(delivery.getBody(), StandardCharsets.UTF_8);
            try {
                MailboxActionPayload payload = new ObjectMapper().readValue(json, MailboxActionPayload.class);
                boolean success = actionHandler.handle(payload);
                actionHandler.publishResult(payload.getHashID(), success);
            } catch (Exception e) {
                actionHandler.publishResult(extractHashId(json), false);
            }
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }

    private String extractHashId(String json) {
        try {
            return new ObjectMapper().readTree(json).get("hashID").asText();
        } catch (Exception e) {
            return "UNKNOWN_HASH";
        }
    }
}
