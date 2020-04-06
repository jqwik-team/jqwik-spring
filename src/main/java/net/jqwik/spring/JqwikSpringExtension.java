package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.apiguardian.api.*;
import org.springframework.test.context.*;

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
		registrar.register(AroundSpringTestContainer.class, PropagationMode.NO_DESCENDANTS);
		registrar.register(OutsideLifecycleMethodsHook.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(InsideLifecycleMethodsHook.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(ResolveSpringParametersHook.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(EnabledIfHook.class, PropagationMode.DIRECT_DESCENDANTS);
		registrar.register(DisabledIfHook.class, PropagationMode.DIRECT_DESCENDANTS);
	}

}

