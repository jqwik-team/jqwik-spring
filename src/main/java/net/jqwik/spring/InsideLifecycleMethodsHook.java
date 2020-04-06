package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.lifecycle.*;
import org.springframework.test.context.*;

class InsideLifecycleMethodsHook implements AroundTryHook {

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to methods
		return optionalElement.map(element -> element instanceof Method).orElse(false);
	}

	@Override
	public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters) throws Exception {
		Class<?> containerClass = context.containerClass();
		TestContextManager testContextManager = JqwikSpringExtension.getTestContextManager(containerClass);

		Object testInstance = context.testInstance();
		Method testMethod = context.targetMethod();

		beforeExecution(testContextManager, testInstance, testMethod);

		Throwable testException = null;
		try {
			TryExecutionResult executionResult = aTry.execute(parameters);
			testException = executionResult.throwable().orElse(null);
			return executionResult;
		} finally {
			afterExecution(testContextManager, testInstance, testMethod, testException);
		}
	}

	@Override
	public int aroundTryProximity() {
		return 100;
	}

	private void beforeExecution(TestContextManager testContextManager, Object testInstance, Method testMethod) throws Exception {
		testContextManager.beforeTestExecution(testInstance, testMethod);
	}

	public void afterExecution(
			TestContextManager testContextManager,
			Object testInstance,
			Method testMethod,
			Throwable testException
	) throws Exception {
		testContextManager.afterTestExecution(testInstance, testMethod, testException);
	}
}
