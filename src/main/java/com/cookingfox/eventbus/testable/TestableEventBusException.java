package com.cookingfox.eventbus.testable;

/**
 * Exception class specific to {@link TestableEventBus}.
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
