package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.lifecycle.*;
import org.springframework.test.context.*;

class OutsideLifecycleMethodsHook implements AroundTryHook {

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
		prepareTestInstance(testContextManager, testInstance);
		Method testMethod = context.targetMethod();
		beforeExecutionHooks(testContextManager, testInstance, testMethod);

		Throwable testException = null;
		try {
			TryExecutionResult executionResult = aTry.execute(parameters);
			testException = executionResult.throwable().orElse(null);
			return executionResult;
		} finally {
			afterExecutionHooks(testContextManager, testInstance, testMethod, testException);
		}
	}

	@Override
	public int aroundTryProximity() {
		return -20;
	}

	private void prepareTestInstance(TestContextManager testContextManager, Object testInstance) throws Exception {
		JqwikSpringReflectionSupport.applyToInstances(testInstance, testContextManager::prepareTestInstance);
	}

	private void beforeExecutionHooks(
			TestContextManager testContextManager,
			Object testInstance,
			Method testMethod
	) throws Exception {
		testContextManager.beforeTestMethod(testInstance, testMethod);
	}

	private void afterExecutionHooks(
			TestContextManager testContextManager,
			Object testInstance,
			Method testMethod,
			Throwable testException
	) throws Exception {
		testContextManager.afterTestMethod(testInstance, testMethod, testException);
	}

}
