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
@SpringBootTest
@AddLifecycleHook(JqwikSpringExtension.class)
public @interface JqwikSpringBootTest {

	@AliasFor(annotation = SpringBootTest.class, attribute = "value")
	String[] value() default {};

	@AliasFor(annotation = SpringBootTest.class, attribute = "properties")
	String[] properties() default {};

	@AliasFor(annotation = SpringBootTest.class, attribute = "args")
	String[] args() default {};

	@AliasFor(annotation = SpringBootTest.class, attribute = "classes")
	Class<?>[] classes() default {};

	@AliasFor(annotation = SpringBootTest.class, attribute = "webEnvironment")
	WebEnvironment webEnvironment() default WebEnvironment.MOCK;

}
