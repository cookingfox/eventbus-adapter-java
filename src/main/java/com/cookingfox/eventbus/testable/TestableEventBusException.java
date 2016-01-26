package com.cookingfox.eventbus.testable;

/**
 * Exception class specific to {@link TestableEventBus}.
 */
public class TestableEventBusException extends RuntimeException {

    public TestableEventBusException(String message) {
        super(message);
    }

    public TestableEventBusException(String message, Throwable cause) {
        super(message, cause);
    }

}
