package com.cookingfox.eventbus.testable;

/**
 * Wraps a posted event and its subscriber. Useful for checking which subscribers accepted an event.
 */
public class PostedEvent<T> {

    public final T event;
    public final Object subscriber;

    public PostedEvent(T event, Object subscriber) {
        this.event = event;
        this.subscriber = subscriber;
    }

    public T getEvent() {
        return event;
    }

    public Object getSubscriber() {
        return subscriber;
    }

}
