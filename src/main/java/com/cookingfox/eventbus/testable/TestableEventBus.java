package com.cookingfox.eventbus.testable;

import com.cookingfox.eventbus.EventBus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Abel de Beer <abel@cookingfox.nl> on 25/01/16.
 */
public class TestableEventBus implements EventBus {

    //----------------------------------------------------------------------------------------------
    // ENUMS
    //----------------------------------------------------------------------------------------------

    public enum MODE {
        ANNOTATION,
        METHOD_NAME
    }

    //----------------------------------------------------------------------------------------------
    // PROPERTIES
    //----------------------------------------------------------------------------------------------

    private final Set<Class<? extends Annotation>> listenerAnnotations = new LinkedHashSet<>();
    private final Set<String> listenerMethodNames = new LinkedHashSet<>();
    private final Map<Class, Set<EventListener>> listenersMap = new LinkedHashMap<>();
    private final MODE mode;
    private final LinkedList<PostedEvent> postedEvents = new LinkedList<>();
    private final Set<Object> registeredSubjects = new LinkedHashSet<>();
    private SubscriberUncaughtExceptionHandler subscriberUncaughtExceptionHandler;

    //----------------------------------------------------------------------------------------------
    // CONSTRUCTORS
    //----------------------------------------------------------------------------------------------

    public TestableEventBus(final MODE mode) {
        this.mode = mode;
    }

    //----------------------------------------------------------------------------------------------
    // PUBLIC METHODS
    //----------------------------------------------------------------------------------------------

    public synchronized void addAnnotation(Class<? extends Annotation> annotation) {
        final ArrayList<Class<? extends Annotation>> annotations = new ArrayList<>();
        annotations.add(annotation);

        addAnnotations(annotations);
    }

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

        listenerAnnotations.addAll(annotations);
    }

    public synchronized void addMethodName(String methodName) {
        addMethodNames(new String[]{methodName});
    }

    public synchronized void addMethodNames(String[] methodNames) {
        addMethodNames(Arrays.asList(methodNames));
    }

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

        listenerMethodNames.addAll(methodNames);
    }

    public synchronized Object getFirstPostedEvent() {
        return postedEvents.isEmpty() ? null : postedEvents.getFirst().event;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T getFirstPostedEvent(Class<? extends T> eventType) {
        for (PostedEvent posted : postedEvents) {
            final Object event = posted.event;

            if (eventType.isInstance(event)) {
                return (T) event;
            }
        }

        return null;
    }

    public synchronized Object getLastPostedEvent() {
        return postedEvents.isEmpty() ? null : postedEvents.getLast().event;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T getLastPostedEvent(Class<? extends T> eventType) {
        // create copy of posted events, so it can be reversed for traversing
        final LinkedList<PostedEvent> reversed = new LinkedList<>(postedEvents);
        Collections.reverse(reversed);

        for (PostedEvent posted : reversed) {
            final Object event = posted.event;

            if (eventType.isInstance(event)) {
                return (T) event;
            }
        }

        return null;
    }

    @Override
    public synchronized void post(final Object event) {
        if (event == null) {
            throw new TestableEventBusException("Event can not be null");
        }

        final Class eventClass = event.getClass();
        final Set<EventListener> listeners = listenersMap.get(eventClass);

        if (listeners == null) {
            throw new TestableEventBusException("No listeners for event type " + eventClass.getName());
        }

        for (EventListener listener : listeners) {
            final Object subscriber = listener.subscriber;

            try {
                listener.method.invoke(subscriber, event);

                postedEvents.add(new PostedEvent(event, subscriber));
            } catch (Exception e) {
                if (subscriberUncaughtExceptionHandler == null) {
                    throw new TestableEventBusException("Exception during invocation of listener - use " +
                            "`setSubscriberUncaughtExceptionHandler` to handle uncaught " +
                            "subscriber exceptions", e);
                } else {
                    subscriberUncaughtExceptionHandler.handleException(e);
                }
            }
        }
    }

    @Override
    public synchronized void register(final Object subscriber) {
        if (subscriber == null) {
            throw new TestableEventBusException("Subject can not be null");
        }

        if (mode == MODE.ANNOTATION && listenerAnnotations.isEmpty()) {
            throw new TestableEventBusException("You should first add subscriber annotations");
        } else if (mode == MODE.METHOD_NAME && listenerMethodNames.isEmpty()) {
            throw new TestableEventBusException("You should first add subscriber method names");
        }

        if (registeredSubjects.contains(subscriber)) {
            throw new TestableEventBusException("Already registered: " + subscriber);
        }

        final Method[] subjectMethods = subscriber.getClass().getDeclaredMethods();
        final Set<EventListener> listeners = new LinkedHashSet<>();

        for (Method method : subjectMethods) {
            final String name = method.getName();

            switch (mode) {
                case ANNOTATION:
                    boolean hasAnnotation = false;

                    for (Class<? extends Annotation> annotation : listenerAnnotations) {
                        if (method.getAnnotation(annotation) != null) {
                            hasAnnotation = true;
                            break;
                        }
                    }

                    if (!hasAnnotation) {
                        continue;
                    }
                    break;

                case METHOD_NAME:
                    if (!listenerMethodNames.contains(name)) {
                        continue;
                    }
                    break;

                default:
                    throw new TestableEventBusException("Unsupported mode: " + mode);
            }

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

            listeners.add(new EventListener(subscriber, method, eventClass));
        }

        if (listeners.isEmpty()) {
            throw new TestableEventBusException("No event handler methods in subscriber: " + subscriber);
        }

        for (EventListener listener : listeners) {
            Set<EventListener> listenersForEvent = listenersMap.get(listener.eventClass);

            if (listenersForEvent == null) {
                listenersForEvent = new LinkedHashSet<>();
                listenersForEvent.add(listener);
                listenersMap.put(listener.eventClass, listenersForEvent);
            }
        }

        // should be last
        registeredSubjects.add(subscriber);
    }

    public void setSubscriberUncaughtExceptionHandler(SubscriberUncaughtExceptionHandler handler) {
        if (handler == null) {
            throw new TestableEventBusException("Handler can not be null");
        }

        if (subscriberUncaughtExceptionHandler != null) {
            throw new TestableEventBusException("SubscriberUncaughtExceptionHandler is already set");
        }

        subscriberUncaughtExceptionHandler = handler;
    }

    @Override
    public synchronized void unregister(final Object subscriber) {
        if (!registeredSubjects.contains(subscriber)) {
            throw new TestableEventBusException("Subscriber is not registered");
        }

        for (Set<EventListener> listeners : listenersMap.values()) {
            for (EventListener listener : listeners) {
                if (listener.subscriber.equals(subscriber)) {
                    listeners.remove(listener);
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // INNER CLASSES
    //----------------------------------------------------------------------------------------------

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

    static class PostedEvent {

        public final Object event;
        public final Object subscriber;

        public PostedEvent(Object event, Object subscriber) {
            this.event = event;
            this.subscriber = subscriber;
        }

    }

}
