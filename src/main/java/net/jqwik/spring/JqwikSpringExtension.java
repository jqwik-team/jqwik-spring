package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.apiguardian.api.*;
import org.springframework.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.support.*;
import org.springframework.util.*;

/**
 * This class includes all the jqwik hooks necessary to use spring in examples and properties
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1.0")
public class JqwikSpringExtension implements BeforeContainerHook, AfterContainerHook {

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
				containerClass -> Store.create(
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

	/**
	 * Delegates to {@link TestContextManager#prepareTestInstance}.
	 */
//	@Override
//	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
//		getTestContextManager(context).prepareTestInstance(testInstance);
//	}

	/**
	 * Delegates to {@link TestContextManager#beforeTestMethod}.
	 */
//	@Override
//	public void beforeEach(ExtensionContext context) throws Exception {
//		Object testInstance = context.getRequiredTestInstance();
//		Method testMethod = context.getRequiredTestMethod();
//		getTestContextManager(context).beforeTestMethod(testInstance, testMethod);
//	}

	/**
	 * Delegates to {@link TestContextManager#beforeTestExecution}.
	 */
//	@Override
//	public void beforeTestExecution(ExtensionContext context) throws Exception {
//		Object testInstance = context.getRequiredTestInstance();
//		Method testMethod = context.getRequiredTestMethod();
//		getTestContextManager(context).beforeTestExecution(testInstance, testMethod);
//	}

	/**
	 * Delegates to {@link TestContextManager#afterTestExecution}.
	 */
//	@Override
//	public void afterTestExecution(ExtensionContext context) throws Exception {
//		Object testInstance = context.getRequiredTestInstance();
//		Method testMethod = context.getRequiredTestMethod();
//		Throwable testException = context.getExecutionException().orElse(null);
//		getTestContextManager(context).afterTestExecution(testInstance, testMethod, testException);
//	}

	/**
	 * Delegates to {@link TestContextManager#afterTestMethod}.
	 */
//	@Override
//	public void afterEach(ExtensionContext context) throws Exception {
//		Object testInstance = context.getRequiredTestInstance();
//		Method testMethod = context.getRequiredTestMethod();
//		Throwable testException = context.getExecutionException().orElse(null);
//		getTestContextManager(context).afterTestMethod(testInstance, testMethod, testException);
//	}

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
