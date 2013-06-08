package com.ekuefler.supereventbus.shared.priority;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * Defines the priority of a handler method annotated with
 * {@link com.ekuefler.supereventbus.shared.Subscribe}. Handlers with a higher priority are always
 * invoked before handlers with a lower priority. The order in which handlers of the same priority
 * are invoked is undefined. If a priority is not specified with this annotation, it will be given a
 * priority of zero. For example, defining the following three handlers and posting MyEvent would
 * cause the alters "three", "two", and "one" to be shown, in that order:
 *
 * <pre>
 * &#064;Subscribe &#064;WithPriority(-1)
 * void after(MyEvent event) {
 *   Window.alert("one");
 * }
 *
 * &#064;Subscribe
 * void normal(MyEvent event) {
 *   Window.alert("two");
 * }
 *
 * &#064;Subscribe &#064;WithPriority(1)
 * void before(MyEvent event) {
 *   Window.alert("three");
 * }
 * </pre>
 *
 * In general it is best to minimize usage of this annotation such that each handler can operate
 * independently of other handlers. Priorities should only be set for handlers that must explicitly
 * run before or after the majority of handlers, such as loggers that record when events start and
 * finish being processed.
 *
 * @author ekuefler@google.com (Erik Kuefler)
 * @see com.ekuefler.supereventbus.shared.Subscribe
 */
@Documented
@Inherited
@Target(value = ElementType.METHOD)
public @interface WithPriority {
  /**
   * The priority to use for this handler. Higher-priority events are always handled before
   * lower-priority events, with the default priority being 0. Note that priorities may be negative
   * to force handlers to be run after other handlers.
   */
  int value();
}
