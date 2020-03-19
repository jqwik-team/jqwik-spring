package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.apiguardian.api.*;
import org.springframework.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.support.*;

/**
 * This class includes all the jqwik hooks necessary to use spring in examples and properties
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1.0")
public class JqwikSpringExtension implements BeforeContainerHook, AfterContainerHook, AroundTryHook {

	@Override
	public PropagationMode propagateTo() {
		return PropagationMode.DIRECT_DESCENDANTS;
	}

	private Store<TestContextManager> testContextManager;

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to container classes and methods
		return optionalElement.map(element -> (element instanceof Class) || (element instanceof Method)).orElse(false);
	}

	@Override
	public void prepareFor(LifecycleContext context) {
		testContextManager = testContextManagerStore(context);
	}

	private Store<TestContextManager> testContextManagerStore(LifecycleContext context) {
		Optional<Class<?>> optionalContainerClass = optionalContainerClass(context);
		return optionalContainerClass.map(
				containerClass -> Store.getOrCreate(
						Tuple.of(this, containerClass),
						Lifespan.RUN,
						() -> new TestContextManager(containerClass)
				)).orElse(null);
	}

	private Optional<Class<?>> optionalContainerClass(LifecycleContext context) {
		if (context instanceof ContainerLifecycleContext) {
			ContainerLifecycleContext containerLifecycleContext = (ContainerLifecycleContext) context;
			return containerLifecycleContext.containerClass();
		}
		if (context instanceof PropertyLifecycleContext) {
			PropertyLifecycleContext propertyLifecycleContext = (PropertyLifecycleContext) context;
			return Optional.of(propertyLifecycleContext.containerClass());
		}
		if (context instanceof TryLifecycleContext) {
			TryLifecycleContext propertyLifecycleContext = (TryLifecycleContext) context;
			return Optional.of(propertyLifecycleContext.propertyContext().containerClass());
		}
		return Optional.empty();
	}

	private TestContextManager getTestContextManager() {
		return testContextManager.get();
	}

	@Override
	public void beforeContainer(ContainerLifecycleContext context) throws Exception {
		getTestContextManager().beforeTestClass();
	}

	@Override
	public void afterContainer(ContainerLifecycleContext context) throws Exception {
		getTestContextManager().afterTestClass();
	}

	@Override
	public int beforeContainerProximity() {
		return -20;
	}

	@Override
	public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters) throws Exception {
		Object testInstance = context.propertyContext().testInstance();
		prepareTestInstance(testInstance);
		Method testMethod = context.propertyContext().targetMethod();
		beforeExecutionHooks(testInstance, testMethod);

		// TODO: Should run with high proximity (closer than normal hooks), maybe 100:
		beforeExecution(testInstance, testMethod);

		Throwable testException = null;
		try {
			TryExecutionResult executionResult = aTry.execute(parameters);
			testException = executionResult.throwable().orElse(null);
			return executionResult;
		} finally {
			// TODO: Should run with high proximity (closer than normal hooks), maybe 100:
			afterExecution(testInstance, testMethod, testException);

			afterExecutionHooks(testInstance, testMethod, testException);
		}
	}

	@Override
	public int aroundTryProximity() {
		return -20;
	}

	private void prepareTestInstance(Object testInstance) throws Exception {
		getTestContextManager().prepareTestInstance(testInstance);
	}

	private void beforeExecutionHooks(Object testInstance, Method testMethod) throws Exception {
		getTestContextManager().beforeTestMethod(testInstance, testMethod);
	}

	private void beforeExecution(Object testInstance, Method testMethod) throws Exception {
		getTestContextManager().beforeTestExecution(testInstance, testMethod);
	}

	public void afterExecution(Object testInstance, Method testMethod, Throwable testException) throws Exception {
		getTestContextManager().afterTestExecution(testInstance, testMethod, testException);
	}

	private void afterExecutionHooks(Object testInstance, Method testMethod, Throwable testException) throws Exception {
		getTestContextManager().afterTestMethod(testInstance, testMethod, testException);
	}

	/**
	 * Determine if the value for the {@link Parameter} in the supplied {@link ParameterContext}
	 * should be autowired from the test's {@link ApplicationContext}.
	 * <p>A parameter is considered to be autowirable if one of the following
	 * conditions is {@code true}.
	 * <ol>
	 * <li>The {@linkplain ParameterContext#getDeclaringExecutable() declaring
	 * executable} is a {@link Constructor} and
	 * {@link TestConstructorUtils#isAutowirableConstructor(Constructor, Class)}
	 * returns {@code true}.</li>
	 * <li>The parameter is of type {@link ApplicationContext} or a sub-type thereof.</li>
	 * <li>{@link ParameterResolutionDelegate#isAutowirable} returns {@code true}.</li>
	 * </ol>
	 * <p><strong>WARNING</strong>: If a test class {@code Constructor} is annotated
	 * with {@code @Autowired} or automatically autowirable (see {@link TestConstructor}),
	 * Spring will assume the responsibility for resolving all parameters in the
	 * constructor. Consequently, no other registered {@link ParameterResolver}
	 * will be able to resolve parameters.
	 * @see #resolveParameter
	 * @see TestConstructorUtils#isAutowirableConstructor(Constructor, Class)
	 * @see ParameterResolutionDelegate#isAutowirable
	 */
//	@Override
//	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
//		Parameter parameter = parameterContext.getParameter();
//		Executable executable = parameter.getDeclaringExecutable();
//		Class<?> testClass = extensionContext.getRequiredTestClass();
//		return (TestConstructorUtils.isAutowirableConstructor(executable, testClass) ||
//						ApplicationContext.class.isAssignableFrom(parameter.getType()) ||
//						ParameterResolutionDelegate.isAutowirable(parameter, parameterContext.getIndex()));
//	}

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
