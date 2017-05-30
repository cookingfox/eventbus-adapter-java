package com.cookingfox.eventbus.adapter;

import com.cookingfox.eventbus.EventBus;

/**
 * Adapter for the GreenRobot EventBus v3. Don't forget to add the library to your dependencies.
 *
 * @see com.cookingfox.eventbus.EventBus
 * @see org.greenrobot.eventbus.EventBus
 */
public class GreenRobot3EventBusAdapter implements EventBus {

    private final org.greenrobot.eventbus.EventBus eventBus;

    public GreenRobot3EventBusAdapter(org.greenrobot.eventbus.EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void post(Object event) {
        eventBus.post(event);
    }

    @Override
    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    @Override
    public void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }

}
