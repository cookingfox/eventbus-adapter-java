package com.cookingfox.eventbus.testable;

/**
 * Created by Abel de Beer <abel@cookingfox.nl> on 25/01/16.
 */
public interface SubscriberUncaughtExceptionHandler {
    void handleException(Exception e);
}
