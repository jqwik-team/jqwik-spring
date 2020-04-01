package net.jqwik.spring.boot;

import java.lang.annotation.*;

import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.springframework.boot.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.context.SpringBootTest.*;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.*;
import org.springframework.core.env.*;
import org.springframework.test.context.*;

/**
 * Annotation that can be used on a test container class that runs
 * jqwik properties/examples for Spring Boot components.
 *
 * This works - basically - like {@link SpringBootTest}
 * but for jqwik properties and examples.
 *
 * @see JqwikSpringExtension
 * @see ContextConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@AddLifecycleHook(JqwikSpringExtension.class)
public @interface JqwikSpringBootTest {

	/**
	 * Alias for {@link #properties()}.
	 *
	 * @return the properties to apply
	 */
	@AliasFor("properties")
	String[] value() default {};

	/**
	 * Properties in form {@literal key=value} that should be added to the Spring
	 * {@link Environment} before the test runs.
	 *
	 * @return the properties to add
	 */
	@AliasFor("value")
	String[] properties() default {};

	/**
	 * Application arguments that should be passed to the application under test.
	 *
	 * @return the application arguments to pass to the application under test.
	 * @see ApplicationArguments
	 * @see SpringApplication#run(String...)
	 */
	String[] args() default {};

	/**
	 * The <em>component classes</em> to use for loading an
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}. Can also
	 * be specified using
	 * {@link ContextConfiguration#classes() @ContextConfiguration(classes=...)}. If no
	 * explicit classes are defined the test will look for nested
	 * {@link Configuration @Configuration} classes, before falling back to a
	 * {@link SpringBootConfiguration @SpringBootConfiguration} search.
	 *
	 * @return the component classes used to load the application context
	 * @see ContextConfiguration#classes()
	 */
	Class<?>[] classes() default {};

	/**
	 * The type of web environment to create when applicable. Defaults to
	 * {@link WebEnvironment#MOCK}.
	 *
	 * @return the type of web environment
	 */
	WebEnvironment webEnvironment() default WebEnvironment.MOCK;

}
