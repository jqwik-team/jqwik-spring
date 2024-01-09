package net.jqwik.spring;

import java.util.*;

import org.apiguardian.api.*;
import org.springframework.context.*;
import org.springframework.test.context.*;

import net.jqwik.api.lifecycle.*;

/**
 * This class provides convenience methods for Jqwik Lifecycle Hooks
 *
 * @see LifecycleHook
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.12")
public class JqwikSpringLifecycleSupport {

	private JqwikSpringLifecycleSupport() {}

	/**
	 * Returns the {@link ApplicationContext} for the given {@link LifecycleContext}.
	 * If there is none, an empty {@link Optional} is returned.
	 *
	 * @param context A jqwik lifecycle context object
	 * @return The optional {@link ApplicationContext} for the given {@link LifecycleContext}
	 */
	public static Optional<ApplicationContext> applicationContext(LifecycleContext context) {
		return context.optionalContainerClass().flatMap(containerClass -> {
			TestContextManager testContextManager = JqwikSpringExtension.getTestContextManager(containerClass);
			if (testContextManager != null) {
				return Optional.of(testContextManager.getTestContext().getApplicationContext());
			} else {
				return Optional.empty();
			}
		});
	}

}
