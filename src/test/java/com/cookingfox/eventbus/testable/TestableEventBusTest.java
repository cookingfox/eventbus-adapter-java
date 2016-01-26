package com.cookingfox.eventbus.testable;

import org.junit.Test;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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

    @Test(expected = TestableEventBusException.class)
    public void addAnnotations_should_throw_if_null() throws Exception {
        Collection<Class<? extends Annotation>> collection = new ArrayList<>();
        collection.add(null);

        eventBus = createDefaultAnnotationInstance();
        eventBus.addAnnotations(collection);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = TestableEventBusException.class)
    public void addAnnotations_should_throw_if_not_annotation() throws Exception {
        Collection collection = new ArrayList<>();
        collection.add(String.class);

        eventBus = createDefaultAnnotationInstance();
        eventBus.addAnnotations(collection);
    }

    @Test(expected = TestableEventBusException.class)
    public void addAnnotations_should_throw_if_incorrect_mode() throws Exception {
        Collection<Class<? extends Annotation>> collection = new ArrayList<>();
        collection.add(DefaultAnnotation.class);

        eventBus = createDefaultMethodNameInstance();
        eventBus.addAnnotations(collection);
    }

    @Test(expected = TestableEventBusException.class)
    public void addAnnotations_should_throw_if_empty() throws Exception {
        eventBus = createDefaultAnnotationInstance();
        eventBus.addAnnotations(new ArrayList<Class<? extends Annotation>>());
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: addMethodNames
    //----------------------------------------------------------------------------------------------

    @Test(expected = TestableEventBusException.class)
    public void addMethodNames_should_throw_if_name_null() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.addMethodNames(Arrays.asList(new String[]{null}));
    }

    @Test(expected = TestableEventBusException.class)
    public void addMethodNames_should_throw_if_name_empty() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.addMethodNames(Arrays.asList(new String[]{""}));
    }

    @Test(expected = TestableEventBusException.class)
    public void addMethodNames_should_throw_if_incorrect_mode() throws Exception {
        eventBus = createDefaultAnnotationInstance();
        eventBus.addMethodNames(Arrays.asList(new String[]{DEFAULT_METHOD_NAME}));
    }

    @Test(expected = TestableEventBusException.class)
    public void addMethodNames_should_throw_if_empty() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.addMethodNames(new String[]{});
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: clearPostedEvents
    //----------------------------------------------------------------------------------------------

    @Test
    public void clearPostedEvents_should_clear_all_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        eventBus.register(new MultipleListeners());

        eventBus.post(new MyEvent());
        eventBus.post(new MyOtherEvent());
        eventBus.post(new MyEvent());
        eventBus.post(new MyOtherEvent());

        eventBus.clearPostedEvents();

        Collection result = eventBus.getAllPostedEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: getAllPostedEvents (no type)
    //----------------------------------------------------------------------------------------------

    @Test
    public void getAllPostedEvents_noType_should_return_empty_list_for_none_posted() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        Collection<PostedEvent> result = eventBus.getAllPostedEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getAllPostedEvents_noType_should_return_all_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        eventBus.register(new MultipleListeners());

        final Collection<Object> events = new LinkedList<>();
        events.add(new MyEvent());
        events.add(new MyOtherEvent());
        events.add(new MyEvent());
        events.add(new MyOtherEvent());

        for (Object event : events) {
            eventBus.post(event);
        }

        Collection<PostedEvent> result = eventBus.getAllPostedEvents();

        assertEquals(4, result.size());
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: getAllPostedEvents (no type)
    //----------------------------------------------------------------------------------------------

    @Test
    public void getAllPostedEvents_withType_should_return_empty_list_for_none_posted() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        eventBus.register(new MultipleListeners());
        eventBus.post(new MyOtherEvent());
        eventBus.post(new MyOtherEvent());

        Collection<PostedEvent> result = eventBus.getAllPostedEvents(MyEvent.class);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getAllPostedEvents_withType_should_return_all_events_with_type() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        eventBus.register(new MultipleListeners());

        final MyEvent first = new MyEvent();
        final MyEvent second = new MyEvent();

        final Collection<MyEvent> events = new LinkedList<>();
        events.add(first);
        events.add(second);

        for (Object event : events) {
            eventBus.post(event);
            eventBus.post(new MyOtherEvent());
        }

        Collection<PostedEvent> result = eventBus.getAllPostedEvents(MyEvent.class);

        assertEquals(2, result.size());
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: getFirstPostedEvent (no type)
    //----------------------------------------------------------------------------------------------

    @Test
    public void getFirstPostedEvent_noType_should_return_null_if_no_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        PostedEvent result = eventBus.getFirstPostedEvent();

        assertNull(result);
    }

    @Test
    public void getFirstPostedEvent_noType_should_return_first_posted_event() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final MyEvent first = new MyEvent();

        eventBus.register(new MultipleListeners());
        eventBus.post(first);
        eventBus.post(new MyEvent());
        eventBus.post(new MyOtherEvent());

        PostedEvent result = eventBus.getFirstPostedEvent();

        assertSame(first, result.event);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: getFirstPostedEvent (with type)
    //----------------------------------------------------------------------------------------------

    @Test
    public void getFirstPostedEvent_withType_should_return_null_if_no_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        PostedEvent result = eventBus.getFirstPostedEvent(MyEvent.class);

        assertNull(result);
    }

    @Test
    public void getFirstPostedEvent_withType_should_return_first_posted_event() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final MyEvent first = new MyEvent();

        eventBus.register(new MultipleListeners());

        eventBus.post(new MyOtherEvent());
        eventBus.post(first);
        eventBus.post(new MyOtherEvent());
        eventBus.post(new MyEvent());

        PostedEvent result = eventBus.getFirstPostedEvent(MyEvent.class);

        assertSame(first, result.event);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: getLastPostedEvent (no type)
    //----------------------------------------------------------------------------------------------

    @Test
    public void getLastPostedEvent_noType_should_return_null_if_no_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        PostedEvent result = eventBus.getLastPostedEvent();

        assertNull(result);
    }

    @Test
    public void getLastPostedEvent_noType_should_return_last_posted_event() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final MyEvent last = new MyEvent();

        eventBus.register(new MultipleListeners());
        eventBus.post(new MyEvent());
        eventBus.post(new MyOtherEvent());
        eventBus.post(last);

        PostedEvent result = eventBus.getLastPostedEvent();

        assertSame(last, result.event);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: getLastPostedEvent (with type)
    //----------------------------------------------------------------------------------------------

    @Test
    public void getLastPostedEvent_withType_should_return_null_if_no_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        PostedEvent result = eventBus.getLastPostedEvent(MyEvent.class);

        assertNull(result);
    }

    @Test
    public void getLastPostedEvent_withType_should_return_last_posted_event() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final MyEvent last = new MyEvent();

        eventBus.register(new MultipleListeners());
        eventBus.post(new MyEvent());
        eventBus.post(new MyOtherEvent());
        eventBus.post(last);
        eventBus.post(new MyOtherEvent());

        PostedEvent result = eventBus.getLastPostedEvent(MyEvent.class);

        assertSame(last, result.event);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: hasPostedEvents (no type)
    //----------------------------------------------------------------------------------------------

    @Test
    public void hasPostedEvents_noType_should_return_false_for_no_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        boolean result = eventBus.hasPostedEvents();

        assertFalse(result);
    }

    @Test
    public void hasPostedEvents_noType_should_return_true_for_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.register(new MyEventListener());
        eventBus.post(new MyEvent());

        boolean result = eventBus.hasPostedEvents();

        assertTrue(result);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: hasPostedEvents (with type)
    //----------------------------------------------------------------------------------------------

    @Test
    public void hasPostedEvents_withType_should_return_false_for_no_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        boolean result = eventBus.hasPostedEvents(MyEvent.class);

        assertFalse(result);
    }

    @Test
    public void hasPostedEvents_withType_should_return_false_for_no_posted_events_of_type() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.register(new MultipleListeners());
        eventBus.post(new MyOtherEvent());

        boolean result = eventBus.hasPostedEvents(MyEvent.class);

        assertFalse(result);
    }

    @Test
    public void hasPostedEvents_withType_should_return_true_for_posted_events() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.register(new MyEventListener());
        eventBus.post(new MyEvent());

        boolean result = eventBus.hasPostedEvents(MyEvent.class);

        assertTrue(result);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: post
    //----------------------------------------------------------------------------------------------

    @Test(expected = TestableEventBusException.class)
    public void post_should_throw_if_event_null() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.post(null);
    }

    @Test(expected = TestableEventBusException.class)
    public void post_should_throw_if_no_listeners_for_event() throws Exception {
        eventBus = createDefaultMethodNameInstance();
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

    @Test(expected = TestableEventBusException.class)
    public void post_should_throw_if_no_subscriber_uncaught_exception_handler() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.register(new ThrowingListener());
        eventBus.post(new MyEvent());
    }

    @Test
    public void post_should_call_multiple_listeners_of_same_event_type() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final AtomicInteger counter = new AtomicInteger(0);

        eventBus.register(new MyListener() {
            @Override
            public void onEvent(MyEvent event) {
                counter.incrementAndGet();
            }
        });

        eventBus.register(new MyListener() {
            @Override
            public void onEvent(MyEvent event) {
                counter.incrementAndGet();
            }
        });

        eventBus.post(new MyEvent());

        assertEquals(2, counter.get());
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: register (method name mode)
    //----------------------------------------------------------------------------------------------

    @Test(expected = TestableEventBusException.class)
    public void register_methodName_should_throw_if_subject_null() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.register(null);
    }

    @Test(expected = TestableEventBusException.class)
    public void register_methodName_should_throw_if_no_added_method_names() throws Exception {
        eventBus = new TestableEventBus(TestableEventBus.MODE.METHOD_NAME);

        eventBus.register(new MyEventListener());
    }

    @Test(expected = TestableEventBusException.class)
    public void register_methodName_should_throw_if_no_event_methods() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.register(new NoEventMethods());
    }

    @Test(expected = TestableEventBusException.class)
    public void register_methodName_should_throw_if_listener_method_has_multiple_parameters() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        eventBus.register(new MultipleParamListener() {
            @Override
            public void onEvent(MyEvent event, String otherParam) {
                // no-op
            }
        });
    }

    @Test(expected = TestableEventBusException.class)
    public void register_methodName_should_throw_if_listener_not_public() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        eventBus.register(new PrivateEventListener());
    }

    @Test(expected = TestableEventBusException.class)
    public void register_methodName_should_throw_if_listener_method_has_java_package_event_type() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        eventBus.register(new JavaPackageEventType() {
            @Override
            public void onEvent(Collection event) {
                // no-op
            }
        });
    }

    @Test(expected = TestableEventBusException.class)
    public void register_methodName_should_throw_if_already_registered() throws Exception {
        final MyListener listener = new MyEventListener();

        eventBus = createDefaultMethodNameInstance();
        eventBus.register(listener);
        eventBus.register(listener);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: register (annotation mode)
    //----------------------------------------------------------------------------------------------

    @Test(expected = TestableEventBusException.class)
    public void register_annotation_should_throw_if_subject_null() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        eventBus.register(null);
    }

    @Test(expected = TestableEventBusException.class)
    public void register_annotation_should_throw_if_no_added_annotations() throws Exception {
        eventBus = new TestableEventBus(TestableEventBus.MODE.ANNOTATION);

        eventBus.register(new MyEventListener());
    }

    @Test(expected = TestableEventBusException.class)
    public void register_annotation_should_throw_if_no_listener_annotations() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        eventBus.register(new MyListener() {
            @Override
            public void onEvent(MyEvent event) {
                // no-op
            }
        });
    }

    @Test(expected = TestableEventBusException.class)
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

    @Test(expected = TestableEventBusException.class)
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

    @Test(expected = TestableEventBusException.class)
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

    @Test(expected = TestableEventBusException.class)
    public void register_annotation_should_throw_if_already_registered() throws Exception {
        eventBus = createDefaultAnnotationInstance();

        final MyListener listener = new MyEventListener();

        eventBus.register(listener);
        eventBus.register(listener);
    }

    //----------------------------------------------------------------------------------------------
    // TESTS: setSubscriberUncaughtExceptionHandler
    //----------------------------------------------------------------------------------------------

    @Test(expected = TestableEventBusException.class)
    public void setSubscriberUncaughtExceptionHandler_should_throw_if_null() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.setSubscriberUncaughtExceptionHandler(null);
    }

    @Test(expected = TestableEventBusException.class)
    public void setSubscriberUncaughtExceptionHandler_should_throw_if_already_set() throws Exception {
        final SubscriberUncaughtExceptionHandler handler = new SubscriberUncaughtExceptionHandler() {
            @Override
            public void handle(Exception e) {
                // no-op
            }
        };

        eventBus = createDefaultMethodNameInstance();
        eventBus.setSubscriberUncaughtExceptionHandler(handler);
        eventBus.setSubscriberUncaughtExceptionHandler(handler);
    }

    @Test
    public void setSubscriberUncaughtExceptionHandler_should_handle_uncaught_exception() throws Exception {
        final AtomicReference<Exception> subscriberException = new AtomicReference<>();

        eventBus = createDefaultMethodNameInstance();

        eventBus.setSubscriberUncaughtExceptionHandler(new SubscriberUncaughtExceptionHandler() {
            @Override
            public void handle(Exception e) {
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

    @Test(expected = TestableEventBusException.class)
    public void unregister_should_throw_if_not_registered() throws Exception {
        eventBus = createDefaultMethodNameInstance();
        eventBus.unregister(null);
    }

    @Test
    public void unregister_should_remove_listeners() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final AtomicReference<MyEvent> called = new AtomicReference<>();

        final MyListener listener = new MyEventListener();

        eventBus.register(listener);
        eventBus.unregister(listener);
        eventBus.post(new MyEvent());

        assertNull(called.get());
    }

    @Test
    public void unregister_should_remove_listeners_multiple() throws Exception {
        eventBus = createDefaultMethodNameInstance();

        final AtomicInteger counter = new AtomicInteger(0);

        final MultipleListeners first = new MultipleListeners() {
            @Override
            public void onEvent(MyEvent event) {
                counter.incrementAndGet();
            }

            @Override
            public void onEvent(MyOtherEvent event) {
                counter.incrementAndGet();
            }
        };

        final MultipleListeners second = new MultipleListeners() {
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
    static class MultipleListeners {
        public void onEvent(MyEvent event) {
        }

        public void onEvent(MyOtherEvent event) {
        }
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

    static class MyEventListener implements MyListener {
        @DefaultAnnotation
        @Override
        public void onEvent(MyEvent event) {
            // no-op
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
