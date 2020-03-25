package net.jqwik.spring;

import java.lang.annotation.*;

import net.jqwik.api.lifecycle.*;
import org.springframework.context.*;
import org.springframework.core.annotation.*;
import org.springframework.test.context.*;

/**
 * {@code @SpringJqwikConfig} is a combines
 * {@link AddLifecycleHook @AddLifecycleHook(JqwikSpringExtension.class)} from jqwik with
 * {@link ContextConfiguration @ContextConfiguration} from the
 * <em>Spring TestContext Framework</em>.
 *
 * This works - basically - like {@link org.springframework.test.context.junit.jupiter.SpringJUnitConfig}
 * but for jqwik properties and examples.
 *
 * @see JqwikSpringExtension
 * @see ContextConfiguration
 */
@AddLifecycleHook(JqwikSpringExtension.class)
@ContextConfiguration
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpringJqwikConfig {

	/**
	 * Alias for {@link ContextConfiguration#classes}.
	 */
	@AliasFor(annotation = ContextConfiguration.class, attribute = "classes")
	Class<?>[] value() default {};

	/**
	 * Alias for {@link ContextConfiguration#classes}.
	 */
	@AliasFor(annotation = ContextConfiguration.class)
	Class<?>[] classes() default {};

	/**
	 * Alias for {@link ContextConfiguration#locations}.
	 */
	@AliasFor(annotation = ContextConfiguration.class)
	String[] locations() default {};

	/**
	 * Alias for {@link ContextConfiguration#initializers}.
	 */
	@AliasFor(annotation = ContextConfiguration.class)
	Class<? extends ApplicationContextInitializer<?>>[] initializers() default {};

	/**
	 * Alias for {@link ContextConfiguration#inheritLocations}.
	 */
	@AliasFor(annotation = ContextConfiguration.class)
	boolean inheritLocations() default true;

	/**
	 * Alias for {@link ContextConfiguration#inheritInitializers}.
	 */
	@AliasFor(annotation = ContextConfiguration.class)
	boolean inheritInitializers() default true;

	/**
	 * Alias for {@link ContextConfiguration#name}.
	 */
	@AliasFor(annotation = ContextConfiguration.class)
	String name() default "";

}
