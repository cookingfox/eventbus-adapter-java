package com.cookingfox.eventbus.testable;

/**
 * Created by Abel de Beer <abel@cookingfox.nl> on 25/01/16.
 */
public class TestableEventBusException extends RuntimeException {
    public TestableEventBusException() {
    }

    public TestableEventBusException(String message) {
        super(message);
    }

    public TestableEventBusException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestableEventBusException(Throwable cause) {
        super(cause);
    }

    public TestableEventBusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
