package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.support.*;

class ResolveSpringParametersHook implements ResolveParameterHook {

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

	private static class SpringSupplier implements ParameterSupplier {

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
