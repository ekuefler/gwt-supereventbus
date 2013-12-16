What is SuperEventBus?
----------------------

SuperEventBus is a high-powered event bus for GWT. It is a replacement for the
built-in com.google.web.bindery.event.shared.EventBus.

SuperEventBus is currently an **experimental** API that is subject to change.
Please leave feedback and feature requests in the [issue tracker][0]!

Why do I need a new event bus?
------------------------------

GWT's event bus is a great way to make your application more maintainable by
decoupling components from one another, allowing you to work on one without
affecting the others. However, the built-in event bus has several limitations.
Specifically,

 * It's **inflexible**. All events in GWT's event system must extend an `Event`
   class, requiring you to define a new event for any kind of data that must be
   posted on the event bus. Like Guava's event bus, SuperEventBus allows you to
   post any type of event, allowing you to do things like update model objects
   by posting them directly on the bus. Handlers in GWT's event bus are also
   always active when registered - SuperEventBus allows you to define filters
   that can prevent events from being handled in certain situations (say, when
   the component is not visible.)
 * It's **verbose**. Extending `Event` requires implementing several boilerplate
   methods, and registering handlers usually requires creating clunky anonymous
   classes. SuperEventBus again takes inspiration from Guava, allowing you to
   annotate existing methods with the `@Subscribe` annotation to cause them to
   be automatically registered as event handlers.
 * It's **monomorphic**. A handler registered for a given event type will only
   hear instances of that exact type and none of its subtypes. This requires you
   to register redundant handlers for events that should be handled in similar
   ways. In contrast, SuperEventBus's handlers a fully polymorphic, so a handler
   for a given type will be invoked whenever any subtype of that event is posted.
   This lets you do powerful things, like defining tagging interfaces for events
   that share properties that should be handled in the same way, or even
   registering a handler for `Object` that will receive every event in the
   system. When just a single polymorphic type still isn't enough, SuperEventBus
   also allows the use of MultiEvents to handle many events with unrelated types
   using a single handler method.
 * It's **unpredictable**. GWT's event bus dispatches events using a stack, so
   that when one event fires another event it can be impossible to tell which
   order other handlers will see those events in. There is also no way to tell
   GWT that certain handlers should always be invoked before or after other
   handlers. SuperEventBus uses a queue that guarantees events will always be
   handled in the same order in which they're fired, and allows you to specify
   priorities for handlers to affect the order in which they are invoked.

SuperEventBus addresses all of these problems and more, providing a more powerful
event bus that is easier to use.

How do I use SuperEventBus?
---------------------------

SuperEventBus uses a simple annotation-based interface inspired by [Guava's event
bus][1]. To define a handler method, just annotate a non-private method that 
takes a single argument with the `@Subscribe` annotation like this:

```java
@Subscribe
void handleString(String event) {
  Window.alert("The string " + event + " was posted on the event bus.");
}
```

A class may contain any number of methods annotated with `@Subscribe`. In order
to active them, the class must first declare an event registration interface for
the class:

```java
interface MyRegistration extends EventRegistration<MyClass> {}
```

Then, the class just needs to register itself with an instance of the event bus
(this is usually done in the constructor of the class):

```java
@Inject
public MyClass(MyRegistration registration) {
  eventBus.register(this, registration);
}
```

Note that if you're not using Gin, you can also create MyRegistration directly
via GWT.create instead of injecting it. Once registered, `handleString` will
be invoked whenever a `String` is posted on the event bus, which is done like
this:

```java
eventBus.post("some string");
```

Of course, you aren't restricted to posting strings: handler methods can be
defined for any type and any type can be passed to `post`. In practice, most
users will define their own event types rather than posting existing types
directly.

What else can it do?
--------------------

SuperEventBus contains several advanced features that have no analogue in GWT's
built-in event bus. It's easy to get started with SuperEventBus without these
features, and explore them later as needed.

### Priorities

In addition to `@Subscribe`, handler methods can also be annotated with the
`@WithPriority` annotation, which takes a single integer. Handlers with higher
priorities are always invoked before handlers with lower priorities. Handlers
without the `@WithPriority` annotation are given priority 0, and negative
priorities are allowed. See the [javadoc][2] for more details.

### Filters

Handler methods can also be annotated with the `@When` annotation, which takes
a filter class and causes that handler to be ignored when the filter returns
false. Filter classes extend `EventFilter` and look like this:

```java
class IsVisible implements EventFilter<HasVisibility, Object> {
  @Override
  public boolean accepts(HasVisibility handler, Object event) {
    return handler.isVisible();
  }
}
```

A handler annotated with `@When(IsVisible.class)` would be invoked only if its
containing class was visible at the time the event was posted. Note that the
filter accepts both the handler class as well as the event, so it is possible
to filter based on the properties of either. See the [javadoc][3] for more
details.

### MultiEvents

Since EventBus is polymorphic, it is usually possible to handle many types of
events by defining a handler for a common base class of those events. However,
sometimes it is necessary to handle multiple events of unrelated types. This
can be accomplished by declaring a handler with a paramter of type `MultiEvent`
and annotating it with `EventTypes` as follows:

```java
@Subscribe
void handleMultipleTypes(@EventTypes({String.class, Double.class}) MultiEvent event) {
  if (event instanceof String) {
    Window.alert("Got a string: " + event.getEvent());
  } else if (event instanceof Double) {
    Window.alert("Got a double: " + event.getEvent());
  }
}
```

The given handler would be invoked whenever a `String` or `Double` was posted on
the event bus, and the actual event would be accessible via the `getEvent` method
on `MultiEvent`. See the [javadoc][4] for more details.

### Dead events

If an event is fired that has no registered handlers, SuperEventBus will wrap
that event in a `DeadEvent` and re-fire it. This makes it possible to register a
handler for `DeadEvent` that can do something like log a warning when an event
without a handler is fired, which can help detect misconfiguration issues. Note
that `DeadEvent` will never be fired if a handler for `Object` is registered,
since that handler will receive every event posted on the event bus. See the
[javadoc][5] for more details.

How do I install it?
--------------------

If you're using Maven, you can add the following to your `<dependencies>`
section:

```xml
<dependency>
  <groupId>com.ekuefler.supereventbus</groupId>
  <artifactId>supereventbus</artifactId>
  <version>0.1.0</version>
</dependency>
```

You can also download the [jar][6] directly or check out the source using git
from <https://github.com/ekuefler/gwt-supereventbus.git>.

[0]: https://github.com/ekuefler/gwt-supereventbus/issues
[1]: https://code.google.com/p/guava-libraries/wiki/EventBusExplained
[2]: http://ekuefler.github.io/gwt-supereventbus/javadoc/index.html?com/ekuefler/supereventbus/priority/WithPriority.html
[3]: http://ekuefler.github.io/gwt-supereventbus/javadoc/index.html?com/ekuefler/supereventbus/filtering/When.html
[4]: http://ekuefler.github.io/gwt-supereventbus/javadoc/index.html?com/ekuefler/supereventbus/multievent/MultiEvent.html
[5]: http://ekuefler.github.io/gwt-supereventbus/javadoc/index.html?com/ekuefler/supereventbus/DeadEvent.html
[6]: http://search.maven.org/remotecontent?filepath=com/ekuefler/supereventbus/supereventbus/0.1.0/supereventbus-0.1.0.jar
