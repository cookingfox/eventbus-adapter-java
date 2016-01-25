package com.cookingfox.eventbus.testable;

import com.cookingfox.eventbus.EventBus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Simple event bus implementation that makes testing event-based application flows easier.
 * - Executes all events on the posting thread.
 * - Supports both annotation- and name convention based subscriber methods.
 * - Helper methods such as {@link #getFirstPostedEvent()} and {@link #getLastPostedEvent()}.
 */
public class TestableEventBus implements EventBus {

    //----------------------------------------------------------------------------------------------
    // ENUMS
    //----------------------------------------------------------------------------------------------

    /**
     * Defines whether to use annotation or name convention based subscriber methods.
     */
    public enum MODE {

        /**
         * Use annotation based subscriber methods. Annotations need to be added using
         * {@link #addAnnotation(Class)} or {@link #addAnnotations(Collection)}.
         */
        ANNOTATION,

        /**
         * Use method name convention based subscriber methods. Name conventions need to be added
         * using {@link #addMethodName(String)}, {@link #addMethodNames(String[])} or
         * {@link #addMethodNames(Collection)}.
         */
        METHOD_NAME

    }

    //----------------------------------------------------------------------------------------------
    // PROPERTIES
    //----------------------------------------------------------------------------------------------

    /**
     * {@link EventListener} VOs ordered by their event type.
     */
    private final Map<Class, Set<EventListener>> listenersByEventType = new LinkedHashMap<>();

    /**
     * The selected subscriber mode.
     */
    private final MODE mode;

    /**
     * A log of all the posted events, which can be queried using helper methods.
     */
    private final LinkedList<PostedEvent> postedEvents = new LinkedList<>();

    /**
     * All registered subjects, to avoid duplicate registration.
     */
    private final Set<Object> registeredSubjects = new LinkedHashSet<>();

    /**
     * All added subscriber annotation classes.
     *
     * @see MODE#ANNOTATION
     */
    private final Set<Class<? extends Annotation>> subscriberAnnotations = new LinkedHashSet<>();

    /**
     * All added subscriber methods names.
     *
     * @see MODE#METHOD_NAME
     */
    private final Set<String> subscriberMethodNames = new LinkedHashSet<>();

    /**
     * Handler of uncaught exceptions in subscribers.
     */
    private SubscriberUncaughtExceptionHandler subscriberUncaughtExceptionHandler;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS
    //----------------------------------------------------------------------------------------------

    /**
     * @param mode Defines whether to use annotation or name convention based subscriber methods.
     */
    public TestableEventBus(final MODE mode) {
        this.mode = mode;
    }

    //----------------------------------------------------------------------------------------------
    // PUBLIC METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Add an annotation that should be used for subscriber methods.
     */
    public synchronized void addAnnotation(Class<? extends Annotation> annotation) {
        final ArrayList<Class<? extends Annotation>> annotations = new ArrayList<>();
        annotations.add(annotation);

        addAnnotations(annotations);
    }

    /**
     * Add annotations that should be used for subscriber methods.
     */
    public synchronized void addAnnotations(Collection<Class<? extends Annotation>> annotations) {
        if (mode != MODE.ANNOTATION) {
            throw new TestableEventBusException("Can not add annotations when the selected mode is " + mode);
        } else if (annotations.isEmpty()) {
            throw new TestableEventBusException("Annotations collection is empty");
        }

        for (Class<? extends Annotation> annotation : annotations) {
            if (annotation == null) {
                throw new TestableEventBusException("Annotation can not be null");
            } else if (!Annotation.class.isAssignableFrom(annotation)) {
                throw new TestableEventBusException("Class is not of an annotation");
            }
        }

        subscriberAnnotations.addAll(annotations);
    }

    /**
     * Add a method name (convention) that should be used for subscriber methods.
     */
    public synchronized void addMethodName(String methodName) {
        addMethodNames(new String[]{methodName});
    }

    /**
     * Add method names (convention) that should be used for subscriber methods.
     */
    public synchronized void addMethodNames(String[] methodNames) {
        addMethodNames(Arrays.asList(methodNames));
    }

    /**
     * Add method names (convention) that should be used for subscriber methods.
     */
    public synchronized void addMethodNames(Collection<String> methodNames) {
        if (mode != MODE.METHOD_NAME) {
            throw new TestableEventBusException("Can not add method names when the selected mode is " + mode);
        } else if (methodNames.isEmpty()) {
            throw new TestableEventBusException("Method names collection is empty");
        }

        for (String methodName : methodNames) {
            if (methodName == null || methodName.isEmpty()) {
                throw new TestableEventBusException("Method name can not be null");
            }
        }

        subscriberMethodNames.addAll(methodNames);
    }

    /**
     * Clear the log of posted events.
     */
    public void clearPostedEvents() {
        postedEvents.clear();
    }

    /**
     * Returns all posted events.
     */
    public Collection<PostedEvent> getAllPostedEvents() {
        return postedEvents;
    }

    /**
     * Returns all posted events of a specified type.
     */
    @SuppressWarnings("unchecked")
    public synchronized Collection<PostedEvent> getAllPostedEvents(Class eventType) {
        final Collection<PostedEvent> events = new LinkedList<>();

        for (PostedEvent posted : postedEvents) {
            if (eventType.isInstance(posted.event)) {
                events.add(posted);
            }
        }

        return events;
    }

    /**
     * Returns the first posted event.
     */
    public synchronized PostedEvent getFirstPostedEvent() {
        return postedEvents.isEmpty() ? null : postedEvents.getFirst();
    }

    /**
     * Returns the first posted event of a specified type.
     */
    @SuppressWarnings("unchecked")
    public synchronized PostedEvent getFirstPostedEvent(Class eventType) {
        for (PostedEvent posted : postedEvents) {
            if (eventType.isInstance(posted.event)) {
                return posted;
            }
        }

        return null;
    }

    /**
     * Returns the last posted event.
     */
    public synchronized PostedEvent getLastPostedEvent() {
        return postedEvents.isEmpty() ? null : postedEvents.getLast();
    }

    /**
     * Returns the last posted event of a specified type.
     */
    @SuppressWarnings("unchecked")
    public synchronized PostedEvent getLastPostedEvent(Class eventType) {
        // create copy of posted events, so it can be reversed for traversing
        final LinkedList<PostedEvent> reversed = new LinkedList<>(postedEvents);
        Collections.reverse(reversed);

        for (PostedEvent posted : reversed) {
            if (eventType.isInstance(posted.event)) {
                return posted;
            }
        }

        return null;
    }

    /**
     * Post an event to all subscribers.
     *
     * @param event An event object.
     */
    @Override
    public synchronized void post(final Object event) {
        if (event == null) {
            throw new TestableEventBusException("Event can not be null");
        }

        final Class eventClass = event.getClass();
        final Set<EventListener> listeners = listenersByEventType.get(eventClass);

        if (listeners == null) {
            throw new TestableEventBusException("No listeners for event type " + eventClass.getName());
        }

        for (EventListener listener : listeners) {
            final Object subscriber = listener.subscriber;

            try {
                // invoke the subscriber method
                listener.method.invoke(subscriber, event);

                // log the posted event
                postedEvents.add(new PostedEvent(event, subscriber));
            } catch (Exception e) {
                if (subscriberUncaughtExceptionHandler == null) {
                    throw new TestableEventBusException("Exception during invocation of listener " +
                            "- use `setSubscriberUncaughtExceptionHandler` to handle uncaught " +
                            "subscriber exceptions", e);
                } else {
                    subscriberUncaughtExceptionHandler.handle(e);
                }
            }
        }
    }

    /**
     * Register an event subscriber.
     *
     * @param subscriber The object to subscribe.
     */
    @Override
    public synchronized void register(final Object subscriber) {
        if (subscriber == null) {
            throw new TestableEventBusException("Subject can not be null");
        }

        // mode has no added definition(s)? throw
        if (mode == MODE.ANNOTATION && subscriberAnnotations.isEmpty()) {
            throw new TestableEventBusException("You should first add subscriber annotations");
        } else if (mode == MODE.METHOD_NAME && subscriberMethodNames.isEmpty()) {
            throw new TestableEventBusException("You should first add subscriber method names");
        }

        if (registeredSubjects.contains(subscriber)) {
            throw new TestableEventBusException("Already registered: " + subscriber);
        }

        final Method[] subjectMethods = subscriber.getClass().getDeclaredMethods();
        final Set<EventListener> listeners = new LinkedHashSet<>();

        // extract all subscriber's event listeners
        for (Method method : subjectMethods) {
            // no subscriber: skip
            if (!isSubscriber(method)) {
                continue;
            }

            final Class eventClass = getValidEventType(method);

            listeners.add(new EventListener(subscriber, method, eventClass));
        }

        if (listeners.isEmpty()) {
            throw new TestableEventBusException("No event handler methods in subscriber: " + subscriber);
        }

        /**
         * Store the event listeners by event type (more efficient calling in {@link #post}).
         */
        for (EventListener listener : listeners) {
            Set<EventListener> listenersForEvent = listenersByEventType.get(listener.eventClass);

            if (listenersForEvent == null) {
                listenersForEvent = new LinkedHashSet<>();
                listenersByEventType.put(listener.eventClass, listenersForEvent);
            }

            listenersForEvent.add(listener);
        }

        // should be last
        registeredSubjects.add(subscriber);
    }

    /**
     * Set a handler for uncaught exceptions in event subscribers.
     */
    public void setSubscriberUncaughtExceptionHandler(SubscriberUncaughtExceptionHandler handler) {
        if (handler == null) {
            throw new TestableEventBusException("Handler can not be null");
        }

        if (subscriberUncaughtExceptionHandler != null) {
            throw new TestableEventBusException("SubscriberUncaughtExceptionHandler is already set");
        }

        subscriberUncaughtExceptionHandler = handler;
    }

    /**
     * Unsubscribe from events that are posted on the EventBus.
     *
     * @param subscriber The object to unsubscribe.
     */
    @Override
    public synchronized void unregister(final Object subscriber) {
        if (!registeredSubjects.contains(subscriber)) {
            throw new TestableEventBusException("Subscriber is not registered");
        }

        final Map<EventListener, Set<EventListener>> toRemove = new HashMap<>();

        // collect the listeners that need to be removed
        for (Set<EventListener> listeners : listenersByEventType.values()) {
            for (EventListener listener : listeners) {
                if (listener.subscriber.equals(subscriber)) {
                    toRemove.put(listener, listeners);
                }
            }
        }

        // actually remove the listeners
        for (Map.Entry<EventListener, Set<EventListener>> entry : toRemove.entrySet()) {
            entry.getValue().remove(entry.getKey());
        }
    }

    //----------------------------------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------------------------------

    /**
     * Validates the subscriber method and returns its event type.
     */
    private Class getValidEventType(final Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new TestableEventBusException("Event handler methods must be public");
        }

        final Class[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1) {
            throw new TestableEventBusException("Event handler can only have one parameter: the event object");
        }

        final Class eventClass = parameterTypes[0];
        final Package eventPackage = eventClass.getPackage();

        if (eventPackage != null && eventPackage.getName().startsWith("java.")) {
            throw new TestableEventBusException("Event types from `java.*` package are not allowed");
        }

        return eventClass;
    }

    /**
     * Returns whether the method has subscribers.
     */
    private boolean isSubscriber(final Method method) {
        switch (mode) {
            case ANNOTATION:
                boolean hasAnnotation = false;

                for (Class<? extends Annotation> annotation : subscriberAnnotations) {
                    if (method.getAnnotation(annotation) != null) {
                        hasAnnotation = true;
                        break;
                    }
                }

                if (hasAnnotation) {
                    return true;
                }
                break;

            case METHOD_NAME:
                if (subscriberMethodNames.contains(method.getName())) {
                    return true;
                }
                break;

            default:
                throw new TestableEventBusException("Unsupported subscriber mode: " + mode);
        }

        return false;
    }

    //----------------------------------------------------------------------------------------------
    // INNER CLASSES
    //----------------------------------------------------------------------------------------------

    /**
     * Wraps a subscriber with its event type and listener method.
     */
    static class EventListener {

        public final Class eventClass;
        public final Method method;
        public final Object subscriber;

        public EventListener(Object subscriber, Method method, Class eventClass) {
            this.eventClass = eventClass;
            this.method = method;
            this.subscriber = subscriber;
        }

    }

}
