package net.jqwik.spring;

import java.lang.annotation.*;

import org.apiguardian.api.*;

import net.jqwik.api.lifecycle.*;

import static org.apiguardian.api.API.Status.*;

/**
 * {@code @JqwikSpringSupport} is nothing but an alias for
 * {@link AddLifecycleHook @AddLifecycleHook(JqwikSpringExtension.class)}.
 * Using this as an additional annotation most things should work the same
 * way when using Spring's JUnit5/Jupiter support.
 *
 * @see JqwikSpringExtension
 */
@AddLifecycleHook(JqwikSpringExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@API(status = EXPERIMENTAL, since = "0.6.0")
public @interface JqwikSpringSupport {

}
