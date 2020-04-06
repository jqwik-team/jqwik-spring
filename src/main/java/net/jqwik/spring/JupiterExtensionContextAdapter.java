package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.lifecycle.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

class JupiterExtensionContextAdapter implements ExtensionContext {

	private LifecycleContext context;

	JupiterExtensionContextAdapter(LifecycleContext context) {
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
		return new JupiterStoreAdapter(context);
	}
}
