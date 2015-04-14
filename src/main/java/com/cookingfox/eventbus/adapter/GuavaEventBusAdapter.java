package com.cookingfox.eventbus.adapter;

import com.cookingfox.eventbus.EventBus;

/**
 * Adapter for the Google Guava EventBus. Don't forget to add the library to your dependencies.
 *
 * @see com.cookingfox.eventbus.EventBus
 * @see com.google.common.eventbus.EventBus
 */
public class GuavaEventBusAdapter implements EventBus {

    private com.google.common.eventbus.EventBus eventBus;

    public GuavaEventBusAdapter(com.google.common.eventbus.EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }

}
