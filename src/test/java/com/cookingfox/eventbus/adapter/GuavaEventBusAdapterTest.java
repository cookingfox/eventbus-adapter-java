package com.cookingfox.eventbus.adapter;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertSame;

/**
 * Integration tests for {@link GuavaEventBusAdapter}.
 */
public class GuavaEventBusAdapterTest {

    @Test
    public void should_wrap_guava_eventbus() throws Exception {
        final GuavaEventBusAdapter eventBus = new GuavaEventBusAdapter(new EventBus());
        final AtomicReference<ExampleEvent> receivedEvent = new AtomicReference<ExampleEvent>();
        final ExampleEvent event = new ExampleEvent();

        final ExampleSubscriber subscriber = new ExampleSubscriber() {
            @Subscribe
            public void onEvent(ExampleEvent event) {
                receivedEvent.set(event);
            }
        };

        eventBus.register(subscriber);
        eventBus.post(event);
        eventBus.unregister(subscriber);

        assertSame(event, receivedEvent.get());
    }

    static class ExampleEvent {
    }

    interface ExampleSubscriber {
        @SuppressWarnings("unused")
        void onEvent(ExampleEvent event);
    }

}
