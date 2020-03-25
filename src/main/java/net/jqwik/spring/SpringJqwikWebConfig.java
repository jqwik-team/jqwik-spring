package net.jqwik.spring;

import java.lang.annotation.*;

import net.jqwik.api.lifecycle.*;
import org.apiguardian.api.*;
import org.springframework.context.*;
import org.springframework.core.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.web.*;

import static org.apiguardian.api.API.Status.*;

/**
 * {@code @SpringJqwikWebConfig} is combines
 * {@link AddLifecycleHook @AddLifecycleHook(JqwikSpringExtension.class)} from jqwik with
 * {@link ContextConfiguration @ContextConfiguration} and
 * {@link WebAppConfiguration @WebAppConfiguration} from the
 * <em>Spring TestContext Framework</em>.
 *
 * @see JqwikSpringExtension
 * @see ContextConfiguration
 * @see WebAppConfiguration
 * @see SpringJqwikConfig
 */
@AddLifecycleHook(JqwikSpringExtension.class)
@ContextConfiguration
@WebAppConfiguration
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = EXPERIMENTAL, since = "0.5.0")
public @interface SpringJqwikWebConfig {

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

	/**
	 * Alias for {@link WebAppConfiguration#value}.
	 */
	@AliasFor(annotation = WebAppConfiguration.class, attribute = "value")
	String resourcePath() default "src/main/webapp";

}
