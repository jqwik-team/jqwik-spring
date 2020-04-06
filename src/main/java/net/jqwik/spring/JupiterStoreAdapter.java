package net.jqwik.spring;

import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.test.context.*;

class JupiterStoreAdapter implements ExtensionContext.Store {

	private LifecycleContext context;

	public JupiterStoreAdapter(LifecycleContext context) {
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
