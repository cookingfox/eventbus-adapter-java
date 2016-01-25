package com.cookingfox.eventbus;

/**
 * Gives the implementing class the ability to subscribe and unsubscribe objects to events that are
 * posted on the EventBus.
 */
public interface EventBusSubscriber {

    /**
     * Subscribe for events that are posted on the EventBus.
     *
     * @param subscriber The object to subscribe.
     */
    void register(Object subscriber);

    /**
     * Unsubscribe from events that are posted on the EventBus.
     *
     * @param subscriber The object to unsubscribe.
     */
    void unregister(Object subscriber);

}
