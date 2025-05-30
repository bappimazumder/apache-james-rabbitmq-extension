
package com.mail.ext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.james.mailbox.MailboxManager;
import com.rabbitmq.client.*;
import org.apache.james.mailbox.MessageIdManager;

import org.apache.james.lifecycle.api.Startable;
import org.apache.james.modules.data.MemoryDataModule;

import javax.inject.Inject;

public class RabbitMQMailboxListenerStarter implements Startable {

   private final MailboxManager mailboxManager;
   private final MessageIdManager messageIdManager;

    @Inject
    public RabbitMQMailboxListenerStarter(MailboxManager mailboxManager, MessageIdManager messageIdManager) {
        this.mailboxManager = mailboxManager;
        this.messageIdManager = messageIdManager;
    }

    public void start() throws Exception{
        System.out.printf("Starting RabbitMQMailboxListenerStarter");
        try {
            String rabbitHost = "127.0.0.1";
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            factory.setPort(5672);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declare exchange/queue (optional if already created)
            String exchange = "myExchange";
            String queue = "jsonQueue";
            channel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT, true);
            channel.queueDeclare(queue, true, false, false, null);

            MailboxActionHandler handler = new MailboxActionHandler(
                    mailboxManager,
                    messageIdManager,
                    channel,
                    exchange
            );

           // MailboxActionHandler handler = new MailboxActionHandler();

            RabbitMQMailboxListener listener = new RabbitMQMailboxListener(rabbitHost, handler);
            listener.startListening(queue);

            System.out.println("📡 RabbitMQMailboxListener started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

