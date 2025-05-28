package com.mail.ext;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.apache.james.lifecycle.api.Startable;


public class RabbitMQMailboxExtensionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Startable.class)
                .to(RabbitMQMailboxListenerStarter.class)
                .in(Scopes.SINGLETON);
    }
}
