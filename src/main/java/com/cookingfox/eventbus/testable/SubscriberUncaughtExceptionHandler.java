package com.cookingfox.eventbus.testable;

/**
 * Handle uncaught exceptions in subscribers.
 * NOTE: It is bad practice to have your event subscribers throw exceptions - they should be handled
 * right inside the method.
 *
 * @see TestableEventBus#setSubscriberUncaughtExceptionHandler(SubscriberUncaughtExceptionHandler)
 */
public interface SubscriberUncaughtExceptionHandler {

    /**
     * Handle the uncaught subscriber exception.
     */
    void handle(Exception e);

}
