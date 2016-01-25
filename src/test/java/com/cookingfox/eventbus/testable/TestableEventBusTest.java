package com.cookingfox.eventbus.testable;

import org.junit.Test;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TestableEventBus}.
 */
public class TestableEventBusTest {

    private static final String DEFAULT_EXCEPTION_MESSAGE = "Example exception message";
    private static final String DEFAULT_METHOD_NAME = "onEvent";

    private TestableEventBus eventBus;

    //----------------------------------------------------------------------------------------------
    // TESTS: addAnnotations
    //----------------------------------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void addAnnotations_should_throw_if_null() throws Exception {
        Collection<Class<? extends Annotation>> collection = new ArrayList<>();
        collection.add(null);

        eventBus = new TestableEventBus(TestableEventBus.MODE.ANNOTATION);
        eventBus.addAnnotations(collection);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RuntimeException.class)
    public void addAnnotations_should_throw_if_not_annotation() throws Exception {
        Collection collection = new ArrayList<>();
        collection.add(String.class);

        eventBus = new TestableEventBus(TestableEventBus.MODE.ANNOTATION);
        eventBus.addAnnotations(collection);
    }

    @Test(expected = RuntimeException.class)
    public void addAnnotations_should_throw_if_incorrect_mode() throws Exception {
        Collection<Class<? extends Annotation>> collection = new ArrayList<>();
        collection.add(DefaultAnnotation.class);

        eventBus = new TestableEventBus(TestableEventBus.MODE.METHOD_NAME);
        eventBus.addAnnotations(collection);
    }

    @Test(expected = RuntimeException.class)
    public void addAnnotations_should_throw_if_empty() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        eventBus.addAnnotations(new ArrayList<Class<? extends Annotation>>());
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: addMethodNames
    //----------------------------------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void addMethodNames_should_throw_if_name_null() throws Exception {
        eventBus.addMethodNames(Arrays.asList(new String[]{null}));
    }

    @Test(expected = NullPointerException.class)
    public void addMethodNames_should_throw_if_name_empty() throws Exception {
        eventBus.addMethodNames(Arrays.asList(new String[]{""}));
    }

    @Test(expected = RuntimeException.class)
    public void addMethodNames_should_throw_if_incorrect_mode() throws Exception {
        eventBus = new TestableEventBus(TestableEventBus.MODE.ANNOTATION);
        eventBus.addMethodNames(Arrays.asList(new String[]{DEFAULT_METHOD_NAME}));
    }

    @Test(expected = RuntimeException.class)
    public void addMethodNames_should_throw_if_empty() throws Exception {
        eventBus.addMethodNames(new String[]{});
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: post
    //----------------------------------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void post_should_throw_if_event_null() throws Exception {
        eventBus.post(null);
    }

    @Test(expected = RuntimeException.class)
    public void post_should_throw_if_no_listeners_for_event() throws Exception {
        eventBus.post(new MyEvent());
    }

    @Test
    public void post_should_pass_event_to_method_listener() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final AtomicReference<MyEvent> called = new AtomicReference<>();

        eventBus.register(new MyListener() {
            @Override
            public void onEvent(MyEvent event) {
                called.set(event);
            }
        });

        final MyEvent event = new MyEvent();

        eventBus.post(event);

        assertNotNull(called.get());
        assertSame(event, called.get());
    }

    @Test
    public void post_should_pass_event_to_annotations_listener() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        final AtomicReference<MyEvent> called = new AtomicReference<>();

        eventBus.register(new MyListener() {
            @DefaultAnnotation
            @Override
            public void onEvent(MyEvent event) {
                called.set(event);
            }
        });

        final MyEvent event = new MyEvent();

        eventBus.post(event);

        assertNotNull(called.get());
        assertSame(event, called.get());
    }

    @Test(expected = RuntimeException.class)
    public void post_should_throw_if_no_subscriber_uncaught_exception_handler() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.register(new ThrowingListener());
        eventBus.post(new MyEvent());
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: register (method name mode)
    //----------------------------------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void register_methodName_should_throw_if_subject_null() throws Exception {
        eventBus.register(null);
    }

    @Test(expected = RuntimeException.class)
    public void register_annotation_should_throw_if_no_added_method_names() throws Exception {
        eventBus = new TestableEventBus(TestableEventBus.MODE.METHOD_NAME);

        eventBus.register(new MyListener() {
            @Override
            public void onEvent(MyEvent event) {
                // no-op
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void register_methodName_should_throw_if_no_event_methods() throws Exception {
        eventBus.register(new NoEventMethods());
    }

    @Test(expected = RuntimeException.class)
    public void register_methodName_should_throw_if_listener_method_has_multiple_parameters() throws Exception {
        eventBus.register(new MultipleParamListener() {
            @Override
            public void onEvent(MyEvent event, String otherParam) {
                // no-op
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void register_methodName_should_throw_if_listener_not_public() throws Exception {
        eventBus.register(new PrivateEventListener());
    }

    @Test(expected = RuntimeException.class)
    public void register_methodName_should_throw_if_listener_method_has_java_package_event_type() throws Exception {
        eventBus.register(new JavaPackageEventType() {
            @Override
            public void onEvent(Collection event) {
                // no-op
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void register_methodName_should_throw_if_already_registered() throws Exception {
        final MyListener listener = new MyListener() {
            @Override
            public void onEvent(MyEvent event) {
                // no-op
            }
        };

        eventBus.register(listener);
        eventBus.register(listener);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: register (annotation mode)
    //----------------------------------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void register_annotation_should_throw_if_subject_null() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        eventBus.register(null);
    }

    @Test(expected = RuntimeException.class)
    public void register_annotation_should_throw_if_no_added_annotations() throws Exception {
        eventBus = new TestableEventBus(TestableEventBus.MODE.ANNOTATION);

        eventBus.register(new MyListener() {
            @DefaultAnnotation
            @Override
            public void onEvent(MyEvent event) {
                // no-op
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void register_annotation_should_throw_if_no_listener_annotations() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        eventBus.register(new MyListener() {
            @Override
            public void onEvent(MyEvent event) {
                // no-op
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void register_annotation_should_throw_if_listener_method_has_multiple_parameters() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        eventBus.register(new MultipleParamListener() {
            @DefaultAnnotation
            @Override
            public void onEvent(MyEvent event, String otherParam) {
                // no-op
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void register_annotation_should_throw_if_listener_not_public() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        eventBus.register(new PrivateEventListener() {
            @DefaultAnnotation
            @Override
            protected void onEvent(MyEvent event) {
                super.onEvent(event);
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void register_annotation_should_throw_if_listener_method_has_java_package_event_type() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        eventBus.register(new JavaPackageEventType() {
            @DefaultAnnotation
            @Override
            public void onEvent(Collection event) {
                // no-op
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void register_annotation_should_throw_if_already_registered() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        final MyListener listener = new MyListener() {
            @DefaultAnnotation
            @Override
            public void onEvent(MyEvent event) {
                // no-op
            }
        };

        eventBus.register(listener);
        eventBus.register(listener);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: setSubscriberUncaughtExceptionHandler
    //----------------------------------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void setSubscriberUncaughtExceptionHandler_should_throw_if_null() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        eventBus.setSubscriberUncaughtExceptionHandler(null);
    }

    @Test(expected = RuntimeException.class)
    public void setSubscriberUncaughtExceptionHandler_should_throw_if_already_set() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final SubscriberUncaughtExceptionHandler handler = new SubscriberUncaughtExceptionHandler() {
            @Override
            public void handleException(Exception e) {
                // no-op
            }
        };

        eventBus.setSubscriberUncaughtExceptionHandler(handler);
        eventBus.setSubscriberUncaughtExceptionHandler(handler);
    }

    @Test
    public void setSubscriberUncaughtExceptionHandler_should_handle_uncaught_exception() throws Exception {
        final AtomicReference<Exception> subscriberException = new AtomicReference<>();

        eventBus = createDefaultMethodNameInstance();

        eventBus.setSubscriberUncaughtExceptionHandler(new SubscriberUncaughtExceptionHandler() {
            @Override
            public void handleException(Exception e) {
                subscriberException.set(e);
            }
        });

        eventBus.register(new ThrowingListener());
        eventBus.post(new MyEvent());

        final Exception actualException = subscriberException.get();

        assertNotNull(actualException);
        assertTrue(actualException instanceof InvocationTargetException);
        assertSame(DEFAULT_EXCEPTION_MESSAGE, actualException.getCause().getMessage());
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: unregister
    //----------------------------------------------------------------------------------------------

    @Test(expected = RuntimeException.class)
    public void unregister_should_throw_if_not_registered() throws Exception {
        eventBus.unregister(null);
    }

    @Test
    public void unregister_should_remove_listeners() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final AtomicReference<MyEvent> called = new AtomicReference<>();

        final MyListener listener = new MyListener() {
            @Override
            public void onEvent(MyEvent event) {
                called.set(event);
            }
        };

        eventBus.register(listener);
        eventBus.unregister(listener);
        eventBus.post(new MyEvent());

        assertNull(called.get());
    }

    @Test
    public void unregister_should_remove_listeners_multiple() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final AtomicInteger counter = new AtomicInteger(0);

        MultipleListeners first = new MultipleListeners() {
            @Override
            public void onEvent(MyEvent event) {
                counter.incrementAndGet();
            }

            @Override
            public void onEvent(MyOtherEvent event) {
                counter.incrementAndGet();
            }
        };

        MultipleListeners second = new MultipleListeners() {
            @Override
            public void onEvent(MyEvent event) {
                counter.incrementAndGet();
            }

            @Override
            public void onEvent(MyOtherEvent event) {
                counter.incrementAndGet();
            }
        };

        eventBus.register(first);
        eventBus.register(second);
        eventBus.unregister(first);
        eventBus.unregister(second);
        eventBus.post(new MyEvent());
        eventBus.post(new MyOtherEvent());

        assertEquals(0, counter.get());
    }

    //----------------------------------------------------------------------------------------------
    // HELPERS
    //----------------------------------------------------------------------------------------------

    private TestableEventBus createDefaultAnnotationInstance() {
        final TestableEventBus instance = new TestableEventBus(TestableEventBus.MODE.ANNOTATION);
        instance.addAnnotation(DefaultAnnotation.class);

        return instance;
    }

    private TestableEventBus createDefaultMethodNameInstance() {
        final TestableEventBus instance = new TestableEventBus(TestableEventBus.MODE.METHOD_NAME);
        instance.addMethodName(DEFAULT_METHOD_NAME);

        return instance;
    }

    //----------------------------------------------------------------------------------------------
    // FIXTURES
    //----------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    interface MyListener {
        void onEvent(MyEvent event);
    }

    @SuppressWarnings("unused")
    interface MultipleParamListener {
        void onEvent(MyEvent event, String otherParam);
    }

    @SuppressWarnings("unused")
    interface JavaPackageEventType {
        void onEvent(Collection event);
    }

    @SuppressWarnings("unused")
    interface MultipleListeners {
        void onEvent(MyEvent event);

        void onEvent(MyOtherEvent event);
    }

    @SuppressWarnings("unused")
    static class PrivateEventListener {
        protected void onEvent(MyEvent event) {
            // no-op
        }
    }

    @SuppressWarnings("unused")
    static class ThrowingListener {
        public void onEvent(MyEvent event) throws Exception {
            throw new Exception(DEFAULT_EXCEPTION_MESSAGE);
        }
    }

    static class MyEvent {

    }

    static class MyOtherEvent {

    }

    static class NoEventMethods {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface DefaultAnnotation {
    }

}
