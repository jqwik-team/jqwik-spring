package net.jqwik.spring;

import java.util.*;

import org.apiguardian.api.*;
import org.springframework.context.*;
import org.springframework.test.context.*;

import net.jqwik.api.lifecycle.*;

@API(status = API.Status.EXPERIMENTAL, since = "0.12")
public class JqwikSpringLifecycleSupport {

	private JqwikSpringLifecycleSupport() {}

	public static Optional<ApplicationContext> applicationContext(LifecycleContext context) {
		if (context instanceof MethodLifecycleContext methodContext) {
			TestContextManager testContextManager =
				JqwikSpringExtension.getTestContextManager(methodContext.containerClass());
			if (testContextManager != null) {
				return Optional.of(testContextManager.getTestContext().getApplicationContext());
			}
		}
		return Optional.empty();
	}

}
