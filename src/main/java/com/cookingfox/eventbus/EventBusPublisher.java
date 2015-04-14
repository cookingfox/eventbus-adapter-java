package com.cookingfox.eventbus;

/**
 * Gives the implementing class the ability to post events on the EventBus.
 */
public interface EventBusPublisher {

    /**
     * Posts the given event to the EventBus.
     *
     * @param event An event object.
     */
    void post(Object event);

}
