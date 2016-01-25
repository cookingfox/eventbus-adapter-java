package com.cookingfox.eventbus.adapter;

import com.cookingfox.eventbus.EventBus;

/**
 * Adapter for the GreenRobot EventBus. Don't forget to add the library to your dependencies.
 *
 * @see com.cookingfox.eventbus.EventBus
 * @see de.greenrobot.event.EventBus
 */
public class GreenRobotEventBusAdapter implements EventBus {

    private de.greenrobot.event.EventBus eventBus;

    public GreenRobotEventBusAdapter(de.greenrobot.event.EventBus eventBus) {
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
