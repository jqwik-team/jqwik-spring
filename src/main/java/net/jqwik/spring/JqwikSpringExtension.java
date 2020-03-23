package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.apiguardian.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.test.context.*;

/**
 * This class includes all the jqwik hooks necessary to use spring in examples and properties
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1.0")
public class JqwikSpringExtension implements RegistrarHook, BeforeContainerHook {

	public static Optional<TestContextManager> getTestContextManager(LifecycleContext context) {
		Store<TestContextManager> store = testContextManagerStore(context);
		if (store != null) {
			return Optional.of(store.get());
		}
		return Optional.empty();
	}

	private static Store<TestContextManager> testContextManagerStore(LifecycleContext context) {
		Optional<Class<?>> optionalContainerClass = context.optionalContainerClass();
		return optionalContainerClass.map(
				containerClass -> Store.<TestContextManager>get(storeIdentifier(containerClass))
		).orElse(null);
	}

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to container classes
		return optionalElement.map(element -> element instanceof Class).orElse(false);
	}

	@Override
	public void beforeContainer(ContainerLifecycleContext context) {
		Optional<Class<?>> optionalContainerClass = context.optionalContainerClass();
		optionalContainerClass.ifPresent(
				containerClass -> Store.getOrCreate(
						storeIdentifier(containerClass),
						Lifespan.RUN,
						() -> new TestContextManager(containerClass)
				));
	}

	private static Tuple.Tuple2<?, ?> storeIdentifier(Class<?> containerClass) {
		return Tuple.of(JqwikSpringExtension.class, containerClass);
	}

	@Override
	public int beforeContainerProximity() {
		return -30;
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
		Optional<TestContextManager> optionalTestContextManager = JqwikSpringExtension.getTestContextManager(context);
		if (optionalTestContextManager.isPresent()) {
			optionalTestContextManager.get().beforeTestClass();
		}
	}

	@Override
	public void afterContainer(ContainerLifecycleContext context) throws Exception {
		Optional<TestContextManager> optionalTestContextManager = JqwikSpringExtension.getTestContextManager(context);
		if (optionalTestContextManager.isPresent()) {
			optionalTestContextManager.get().afterTestClass();
		}
	}

	@Override
	public int beforeContainerProximity() {
		return -20;
	}

}

class OutsideHooks implements AroundTryHook {

	private TestContextManager testContextManager;

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to methods
		return optionalElement.map(element -> element instanceof Method).orElse(false);
	}

	@Override
	public void prepareFor(LifecycleContext context) {
		JqwikSpringExtension.getTestContextManager(context).ifPresent(testContextManager -> this.testContextManager = testContextManager);
	}

	@Override
	public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters) throws Exception {
		Object testInstance = context.propertyContext().testInstance();
		prepareTestInstance(testInstance);
		Method testMethod = context.propertyContext().targetMethod();
		beforeExecutionHooks(testInstance, testMethod);

		Throwable testException = null;
		try {
			TryExecutionResult executionResult = aTry.execute(parameters);
			testException = executionResult.throwable().orElse(null);
			return executionResult;
		} finally {
			afterExecutionHooks(testInstance, testMethod, testException);
		}
	}

	@Override
	public int aroundTryProximity() {
		return -20;
	}

	private void prepareTestInstance(Object testInstance) throws Exception {
		testContextManager.prepareTestInstance(testInstance);
	}

	private void beforeExecutionHooks(Object testInstance, Method testMethod) throws Exception {
		testContextManager.beforeTestMethod(testInstance, testMethod);
	}

	private void afterExecutionHooks(Object testInstance, Method testMethod, Throwable testException) throws Exception {
		testContextManager.afterTestMethod(testInstance, testMethod, testException);
	}

}

class InsideHooks implements AroundTryHook {

	private TestContextManager testContextManager;

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to methods
		return optionalElement.map(element -> element instanceof Method).orElse(false);
	}

	@Override
	public void prepareFor(LifecycleContext context) {
		JqwikSpringExtension.getTestContextManager(context).ifPresent(testContextManager -> this.testContextManager = testContextManager);
	}

	@Override
	public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters) throws Exception {
		Object testInstance = context.propertyContext().testInstance();
		Method testMethod = context.propertyContext().targetMethod();

		beforeExecution(testInstance, testMethod);

		Throwable testException = null;
		try {
			TryExecutionResult executionResult = aTry.execute(parameters);
			testException = executionResult.throwable().orElse(null);
			return executionResult;
		} finally {
			afterExecution(testInstance, testMethod, testException);
		}
	}

	@Override
	public int aroundTryProximity() {
		return 100;
	}

	private void beforeExecution(Object testInstance, Method testMethod) throws Exception {
		testContextManager.beforeTestExecution(testInstance, testMethod);
	}

	public void afterExecution(Object testInstance, Method testMethod, Throwable testException) throws Exception {
		testContextManager.afterTestExecution(testInstance, testMethod, testException);
	}
}

class ResolveSpringParameters implements ResolveParameterHook {

	private TestContextManager testContextManager;

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to methods
		return optionalElement.map(element -> element instanceof Method).orElse(false);
	}

	@Override
	public void prepareFor(LifecycleContext context) {
		JqwikSpringExtension.getTestContextManager(context).ifPresent(testContextManager -> this.testContextManager = testContextManager);
	}

	@Override
	public Optional<ParameterSupplier> resolve(ParameterResolutionContext parameterContext) {

		Parameter parameter = parameterContext.parameter();

		if (canParameterBeResolved(parameterContext.index(), parameter)) {
			return Optional.of(new SpringSupplier(parameterContext));
		}

		return Optional.empty();
	}

	private boolean canParameterBeResolved(int index, Parameter parameter) {
		// TODO: Allow autowirable Constructor (see JUnit 5 SpringExtension)
		return ApplicationContext.class.isAssignableFrom(parameter.getType()) ||
					   ParameterResolutionDelegate.isAutowirable(parameter, index);
	}

	private class SpringSupplier implements ResolveParameterHook.ParameterSupplier {

		private ParameterResolutionContext parameterContext;

		public SpringSupplier(ParameterResolutionContext parameterContext) {
			this.parameterContext = parameterContext;
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
				String message = "Trying to resolve Spring parameter outside method context";
				return new JqwikException(message);});
		}
	}

	/**
	 * Resolve a value for the {@link Parameter} in the supplied {@link ParameterContext} by
	 * retrieving the corresponding dependency from the test's {@link ApplicationContext}.
	 * <p>Delegates to {@link ParameterResolutionDelegate#resolveDependency}.
	 * @see #supportsParameter
	 * @see ParameterResolutionDelegate#resolveDependency
	 */
//	@Override
//	@Nullable
//	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
//		Parameter parameter = parameterContext.getParameter();
//		int index = parameterContext.getIndex();
//		Class<?> testClass = extensionContext.getRequiredTestClass();
//		ApplicationContext applicationContext = getApplicationContext(extensionContext);
//		return ParameterResolutionDelegate.resolveDependency(parameter, index, testClass,
//															 applicationContext.getAutowireCapableBeanFactory());
//	}

	/**
	 * Get the {@link ApplicationContext} associated with the supplied {@code ExtensionContext}.
	 * @param context the current {@code ExtensionContext} (never {@code null})
	 * @return the application context
	 * @throws IllegalStateException if an error occurs while retrieving the application context
	 * @see org.springframework.test.context.TestContext#getApplicationContext()
	 */
//	public static ApplicationContext getApplicationContext(ExtensionContext context) {
//		return getTestContextManager(context).getTestContext().getApplicationContext();
//	}

}