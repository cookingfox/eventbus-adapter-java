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

    enum MODE {
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
    private final Map<Class, Set<Object>> postedEvents = new LinkedHashMap<>();
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

    public void addAnnotation(Class<? extends Annotation> annotation) {
        ArrayList<Class<? extends Annotation>> annotations = new ArrayList<>();
        annotations.add(annotation);

        addAnnotations(annotations);
    }

    public void addAnnotations(Collection<Class<? extends Annotation>> annotations) {
        if (mode != MODE.ANNOTATION) {
            throw new RuntimeException("Can not add annotations when the selected mode is " + mode);
        } else if (annotations.isEmpty()) {
            throw new RuntimeException("Annotations collection is empty");
        }

        for (Class<? extends Annotation> annotation : annotations) {
            if (annotation == null) {
                throw new NullPointerException("Annotation can not be null");
            } else if (!Annotation.class.isAssignableFrom(annotation)) {
                throw new RuntimeException("Class is not of an annotation");
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
            throw new RuntimeException("Can not add method names when the selected mode is " + mode);
        } else if (methodNames.isEmpty()) {
            throw new RuntimeException("Method names collection is empty");
        }

        for (String methodName : methodNames) {
            if (methodName == null || methodName.isEmpty()) {
                throw new NullPointerException("Method name can not be null");
            }
        }

        listenerMethodNames.addAll(methodNames);
    }

    @Override
    public synchronized void post(final Object event) {
        final Class eventClass = event.getClass();
        final Set<EventListener> listeners = listenersMap.get(eventClass);

        if (listeners == null) {
            throw new RuntimeException("No listeners for event type " + eventClass.getName());
        }

        for (EventListener listener : listeners) {
            try {
                listener.method.invoke(listener.subscriber, event);

                Set<Object> posted = postedEvents.get(eventClass);

                if (posted == null) {
                    posted = new LinkedHashSet<>();
                    postedEvents.put(eventClass, posted);
                }

                posted.add(event);
            } catch (Exception e) {
                if (subscriberUncaughtExceptionHandler == null) {
                    throw new RuntimeException("Exception during invocation of listener - use " +
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
            throw new NullPointerException("Subject can not be null");
        }

        if (mode == MODE.ANNOTATION && listenerAnnotations.isEmpty()) {
            throw new RuntimeException("You should first add subscriber annotations");
        } else if (mode == MODE.METHOD_NAME && listenerMethodNames.isEmpty()) {
            throw new RuntimeException("You should first add subscriber method names");
        }

        if (registeredSubjects.contains(subscriber)) {
            throw new RuntimeException("Already registered: " + subscriber);
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
                    throw new UnsupportedOperationException("Unsupported mode: " + mode);
            }

            if (!Modifier.isPublic(method.getModifiers())) {
                throw new RuntimeException("Event handler methods must be public");
            }

            final Class[] parameterTypes = method.getParameterTypes();

            if (parameterTypes.length != 1) {
                throw new RuntimeException("Event handler can only have one parameter: the event object");
            }

            final Class eventClass = parameterTypes[0];

            if (eventClass.getPackage().getName().startsWith("java.")) {
                throw new RuntimeException("Event types from `java.*` package are not allowed");
            }

            final EventListener listener = new EventListener(subscriber, method, eventClass);

            listeners.add(listener);
        }

        if (listeners.isEmpty()) {
            throw new RuntimeException("No event handler methods in subscriber: " + subscriber);
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
            throw new NullPointerException("Handler can not be null");
        }

        if (subscriberUncaughtExceptionHandler != null) {
            throw new RuntimeException("SubscriberUncaughtExceptionHandler is already set");
        }

        subscriberUncaughtExceptionHandler = handler;
    }

    @Override
    public synchronized void unregister(final Object subscriber) {
        if (!registeredSubjects.contains(subscriber)) {
            throw new RuntimeException("Subscriber is not registered");
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

    private static class EventListener {

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
