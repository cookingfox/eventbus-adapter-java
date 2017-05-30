# EventBus Adapter for Java

The EventBus Adapter wraps various EventBus implementations for Java and Android. It uses a uniform
interface (`com.cookingfox.eventbus.EventBus`) to type your classes to, so that it's possible to
change the implementation when required.

[![Build Status](https://travis-ci.org/cookingfox/eventbus-adapter-java.svg?branch=master)](https://travis-ci.org/cookingfox/eventbus-adapter-java)

## Download

[![Download](https://api.bintray.com/packages/cookingfox/maven/eventbus-adapter-java/images/download.svg) ](https://bintray.com/cookingfox/maven/eventbus-adapter-java/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.cookingfox/eventbus-adapter-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.cookingfox/eventbus-adapter-java)

The distribution is hosted on [Bintray](https://bintray.com/cookingfox/maven/eventbus-adapter-java/view).
To include the package in your projects, you can add the jCenter repository.

### Gradle

Add jCenter to your `repositories` block:

```groovy
repositories {
    jcenter()
}
```

and add the project to the `dependencies` block in your `build.gradle`:

```groovy
dependencies {
    compile 'com.cookingfox:eventbus-adapter-java:3.0.0'
}
```

### Maven

Add jCenter to your repositories in `pom.xml` or `settings.xml`:

```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>http://jcenter.bintray.com</url>
    </repository>
</repositories>
```

and add the project declaration to your `pom.xml`:

```xml
<dependency>
    <groupId>com.cookingfox</groupId>
    <artifactId>eventbus-adapter-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Features

Currently the library has adapters for:

- [Google Guava EventBus](https://github.com/google/guava)
(tested with version [19.0](http://search.maven.org/#artifactdetails%7Ccom.google.guava%7Cguava%7C19.0%7Cbundle)):
`GuavaEventBusAdapter`

- GreenRobot EventBus:

    - [version 3](https://github.com/greenrobot/EventBus)
    (tested with version [3.0.0](http://search.maven.org/#artifactdetails%7Corg.greenrobot%7Ceventbus%7C3.0.0%7Cjar)):
    `GreenRobot3EventBusAdapter`

    - [version 2](https://github.com/greenrobot/EventBus/tree/v2)
    (tested with version [2.4.1](http://search.maven.org/#artifactdetails%7Cde.greenrobot%7Ceventbus%7C2.4.1%7Cjar)):
    `GreenRobot2EventBusAdapter`

The main `EventBus` interface actually inherits from the `EventBusPublisher` and
`EventBusSubscriber` interfaces. This allows the user to restrict the available functionality of the
consuming class.

## Usage

Include in your project's dependencies:

1. This wrapper library. (see "Download")
2. The library you want to wrap. (see "Features")

Example for the GreenRobot EventBus version 3:

```java
class ExampleEvent {}

class ExampleSubscriber {
    com.cookingfox.eventbus.EventBus eventBus;

    ExampleSubscriber(com.cookingfox.eventbus.EventBus eventBus) {
        this.eventBus = eventBus;
    }

    void onCreate() {
        eventBus.register(this);
    }

    void onEvent(ExampleEvent event) {
        // handle event
    }

    void onDestroy() {
        eventBus.unregister(this);
    }
}

// create real EventBus and adapter
org.greenrobot.eventbus.EventBus realEventBus = new org.greenrobot.eventbus.EventBus();
com.cookingfox.eventbus.EventBus eventBusAdapter = new GreenRobot3Adapter(realEventBus);

// create and register subscriber
ExampleSubscriber subscriber = new ExampleSubscriber(eventBusAdapter);
subscriber.onCreate();

// post an event
eventBusAdapter.post(new ExampleEvent());

// unregister subscriber
subscriber.onDestroy();
```

### Restricting publish / subscribe capabilities

If you want to restrict your consumer classes' EventBus capabilities, use the following interfaces:

- `EventBusPublisher`: the class will only be able to use the `post` method.
- `EventBusSubscriber`: the class will only be able to use the `register` and `unregister` methods.

## F.A.Q.

#### _Is it possible to support EventBus library X?_

If you make an issue for it, we'll take a look at it! :)

## Copyright and license

Code and documentation copyright 2017 Cooking Fox. Code released under the Apache 2.0 license.
