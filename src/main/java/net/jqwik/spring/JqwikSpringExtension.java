package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.apiguardian.api.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.context.support.*;

/**
 * This class includes all the jqwik hooks necessary to use spring in examples and properties
 */
@API(status = API.Status.INTERNAL)
class JqwikSpringExtension implements RegistrarHook {

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
		registrar.register(OutsideHook.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(InsideHook.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(ResolveSpringParameters.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(EnabledIfHook.class, PropagationMode.DIRECT_DESCENDANTS);
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

class OutsideHook implements AroundTryHook {

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

class InsideHook implements AroundTryHook {

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

class ResolveSpringParameters implements ResolveParameterHook {

	@Override
	public Optional<ParameterSupplier> resolve(ParameterResolutionContext parameterContext, LifecycleContext lifecycleContext) {

		Parameter parameter = parameterContext.parameter();
		Class<?> containerClass = parameterContext.parameter().getDeclaringExecutable().getDeclaringClass();
		TestContextManager testContextManager = JqwikSpringExtension.getTestContextManager(containerClass);

		if (canParameterBeResolved(parameterContext.index(), parameter)) {
			return Optional.of(new SpringSupplier(parameterContext, lifecycleContext, testContextManager));
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
		private LifecycleContext lifecycleContext;
		private TestContextManager testContextManager;

		public SpringSupplier(
				ParameterResolutionContext parameterContext,
				LifecycleContext lifecycleContext,
				TestContextManager testContextManager
		) {
			this.parameterContext = parameterContext;
			this.lifecycleContext = lifecycleContext;
			this.testContextManager = testContextManager;
		}

		@Override
		public Object get(Optional<TryLifecycleContext> optionalTry) {
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

class EnabledIfHook implements SkipExecutionHook {

	@Override
	public SkipResult shouldBeSkipped(LifecycleContext context) {
		if (!context.findAnnotation(EnabledIf.class).isPresent()) {
			return SkipResult.doNotSkip();
		}
		ExtensionContext extensionContext = new ExtensionContextAdapter(context);
		ConditionEvaluationResult evaluationResult = new EnabledIfCondition().evaluateExecutionCondition(extensionContext);
		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse(null));
		}
		return SkipResult.doNotSkip();
	}

	private static class ExtensionContextAdapter implements ExtensionContext {

		private LifecycleContext context;

		private ExtensionContextAdapter(LifecycleContext context) {
			this.context = context;
		}

		@Override
		public Optional<ExtensionContext> getParent() {
			return Optional.empty();
		}

		@Override
		public ExtensionContext getRoot() {
			// Used in expression evaluation to get store
			return this;
		}

		@Override
		public String getUniqueId() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getDisplayName() {
			return context.label();
		}

		@Override
		public Set<String> getTags() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<AnnotatedElement> getElement() {
			if (context instanceof PropertyLifecycleContext) {
				return Optional.of(((PropertyLifecycleContext) context).targetMethod());
			}
			if (context instanceof TryLifecycleContext) {
				return Optional.of(((TryLifecycleContext) context).targetMethod());
			}
			return context.optionalElement();
		}

		@Override
		public Optional<Class<?>> getTestClass() {
			return context.optionalContainerClass();
		}

		@Override
		public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
			return Optional.empty();
		}

		@Override
		public Optional<Object> getTestInstance() {
			if (context instanceof PropertyLifecycleContext) {
				return Optional.of(((PropertyLifecycleContext) context).testInstance());
			}
			if (context instanceof TryLifecycleContext) {
				return Optional.of(((TryLifecycleContext) context).testInstance());
			}
			return Optional.empty();
		}

		@Override
		public Optional<TestInstances> getTestInstances() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<Method> getTestMethod() {
			if (context instanceof PropertyLifecycleContext) {
				return Optional.of(((PropertyLifecycleContext) context).targetMethod());
			}
			if (context instanceof TryLifecycleContext) {
				return Optional.of(((TryLifecycleContext) context).targetMethod());
			}
			return Optional.empty();
		}

		@Override
		public Optional<Throwable> getExecutionException() {
			return Optional.empty();
		}

		@Override
		public Optional<String> getConfigurationParameter(String key) {
			return Optional.empty();
		}

		@Override
		public void publishReportEntry(Map<String, String> map) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Store getStore(Namespace namespace) {
			return new StoreAdapter(context);
		}
	}

	private static class StoreAdapter implements org.junit.jupiter.api.extension.ExtensionContext.Store {

		private LifecycleContext context;

		public StoreAdapter(LifecycleContext context) {
			this.context = context;
		}

		@Override
		public Object get(Object key) {
			return JqwikSpringExtension.getTestContextManager(context.optionalContainerClass().orElseThrow(
					() -> new JqwikException("No test context manager registered")
			));
		}

		@Override
		public <V> V get(Object key, Class<V> requiredType) {
			if (requiredType.equals(TestContextManager.class)) {
				return (V) get(key);
			} else {
				return null;
			}
		}

		@Override
		public <K, V> Object getOrComputeIfAbsent(K key, Function<K, V> defaultCreator) {
			return get(key);
		}

		@Override
		public <K, V> V getOrComputeIfAbsent(K key, Function<K, V> defaultCreator, Class<V> requiredType) {
			return get(key, requiredType);
		}

		@Override
		public void put(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object remove(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <V> V remove(Object key, Class<V> requiredType) {
			throw new UnsupportedOperationException();
		}
	}
}