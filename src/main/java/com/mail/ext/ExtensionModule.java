package com.mail.ext;

import org.apache.james.lifecycle.api.Startable;
import org.apache.james.mailbox.MailboxManager;
import com.rabbitmq.client.*;

public class ExtensionModule implements Startable {

    @Override
    public void start() {
        try {
            MailboxManager mailboxManager =  null;// Fetch from James context
            String rabbitHost = "localhost";
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            MailboxActionHandler handler = new MailboxActionHandler(mailboxManager, channel, "action-result-exchange");
            RabbitMQMailboxListener listener = new RabbitMQMailboxListener(rabbitHost, handler);
            listener.startListening("action-request-queue");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
