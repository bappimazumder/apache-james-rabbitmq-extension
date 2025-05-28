package com.mail.ext;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestMain {
    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new RabbitMQMailboxExtensionModule());

        // Example usage: get your Startable class from the injector
        RabbitMQMailboxListenerStarter starter = injector.getInstance(RabbitMQMailboxListenerStarter.class);
        starter.start();
    }
}
