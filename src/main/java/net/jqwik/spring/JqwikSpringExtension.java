package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.apiguardian.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.support.*;

/**
 * This class includes all the jqwik hooks necessary to use spring in examples and properties
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.5.0")
public class JqwikSpringExtension implements RegistrarHook {

	public static TestContextManager getTestContextManager(Class<?> containerClass) {
		return testContextManagerStore(containerClass).get();
	}

	private static Store<TestContextManager> testContextManagerStore(Class<?> containerClass) {
		return getOrCreateTestContextManagerStore(containerClass);
	}

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to container classes
		return optionalElement.map(element -> element instanceof Class).orElse(false);
	}

	private static Store<TestContextManager> getOrCreateTestContextManagerStore(Class<?> containerClass) {
		return Store.getOrCreate(
				storeIdentifier(containerClass),
				Lifespan.RUN,
				() -> new TestContextManager(containerClass)
		);
	}

	private static Tuple.Tuple2<?, ?> storeIdentifier(Class<?> containerClass) {
		return Tuple.of(JqwikSpringExtension.class, containerClass);
	}

	@Override
	public void registerHooks(Registrar registrar) {
		registrar.register(AroundContainer.class, PropagationMode.NO_DESCENDANTS);
		registrar.register(OutsideHooks.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(InsideHooks.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(ResolveSpringParameters.class, PropagationMode.DIRECT_DESCENDANTS);
	}

}

class AroundContainer implements BeforeContainerHook, AfterContainerHook {

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to container classes
		return optionalElement.map(element -> element instanceof Class).orElse(false);
	}

	@Override
	public void beforeContainer(ContainerLifecycleContext context) throws Exception {
		Optional<Class<?>> optionalContainerClass = context.optionalContainerClass();
		if (optionalContainerClass.isPresent()) {
			JqwikSpringExtension.getTestContextManager(optionalContainerClass.get()).beforeTestClass();
		}
	}

	@Override
	public void afterContainer(ContainerLifecycleContext context) throws Exception {
		Optional<Class<?>> optionalContainerClass = context.optionalContainerClass();
		if (optionalContainerClass.isPresent()) {
			JqwikSpringExtension.getTestContextManager(optionalContainerClass.get()).afterTestClass();
		}
	}

	@Override
	public int beforeContainerProximity() {
		return -20;
	}

}

class OutsideHooks implements AroundTryHook {

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to methods
		return optionalElement.map(element -> element instanceof Method).orElse(false);
	}

	@Override
	public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters) throws Exception {
		Class<?> containerClass = context.propertyContext().containerClass();
		TestContextManager testContextManager = JqwikSpringExtension.getTestContextManager(containerClass);

		Object testInstance = context.propertyContext().testInstance();
		prepareTestInstance(testContextManager, testInstance);
		Method testMethod = context.propertyContext().targetMethod();
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
		testContextManager.prepareTestInstance(testInstance);
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

class InsideHooks implements AroundTryHook {

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to methods
		return optionalElement.map(element -> element instanceof Method).orElse(false);
	}

	@Override
	public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters) throws Exception {
		Class<?> containerClass = context.propertyContext().containerClass();
		TestContextManager testContextManager = JqwikSpringExtension.getTestContextManager(containerClass);

		Object testInstance = context.propertyContext().testInstance();
		Method testMethod = context.propertyContext().targetMethod();

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

	public void afterExecution(TestContextManager testContextManager, Object testInstance, Method testMethod, Throwable testException) throws Exception {
		testContextManager.afterTestExecution(testInstance, testMethod, testException);
	}
}

class ResolveSpringParameters implements ResolveParameterHook {

	@Override
	public Optional<ParameterSupplier> resolve(ParameterResolutionContext parameterContext) {

		Parameter parameter = parameterContext.parameter();
		Class<?> containerClass = parameterContext.parameter().getDeclaringExecutable().getDeclaringClass();
		TestContextManager testContextManager = JqwikSpringExtension.getTestContextManager(containerClass);

		if (canParameterBeResolved(parameterContext.index(), parameter)) {
			return Optional.of(new SpringSupplier(parameterContext, testContextManager));
		}

		return Optional.empty();
	}

	private boolean canParameterBeResolved(int index, Parameter parameter) {
		return ApplicationContext.class.isAssignableFrom(parameter.getType()) ||
					   ParameterResolutionDelegate.isAutowirable(parameter, index) ||
					   isAutowirableConstructor(parameter);
	}

	private boolean isAutowirableConstructor(Parameter parameter) {
		Executable executable = parameter.getDeclaringExecutable();
		Class<?> testClass = executable.getDeclaringClass();
		return TestConstructorUtils.isAutowirableConstructor(executable, testClass);
	}

	private static class SpringSupplier implements ResolveParameterHook.ParameterSupplier {

		private ParameterResolutionContext parameterContext;
		private TestContextManager testContextManager;

		public SpringSupplier(
				ParameterResolutionContext parameterContext,
				TestContextManager testContextManager
		) {
			this.parameterContext = parameterContext;
			this.testContextManager = testContextManager;
		}

		@Override
		public Object get(LifecycleContext lifecycleContext) {
			Parameter parameter = parameterContext.parameter();
			int index = parameterContext.index();
			return lifecycleContext.optionalContainerClass().map(testClass -> {
				ApplicationContext applicationContext = testContextManager.getTestContext().getApplicationContext();
				return ParameterResolutionDelegate.resolveDependency(
						parameter,
						index,
						testClass,
						applicationContext.getAutowireCapableBeanFactory()
				);
			}).orElseThrow(() -> {
				String message = String.format(
						"Trying to resolve Spring parameter outside container context: %s",
						lifecycleContext.label()
				);
				return new JqwikException(message);
			});
		}
	}
}