package com.cookingfox.eventbus.testable;

/**
 * Wraps a posted event and its subscriber. Useful for checking which subscribers accepted an event.
 */
public class PostedEvent {

    public final Object event;
    public final Object subscriber;

    public PostedEvent(Object event, Object subscriber) {
        this.event = event;
        this.subscriber = subscriber;
    }

    public Object getEvent() {
        return event;
    }

    public Object getSubscriber() {
        return subscriber;
    }

}
