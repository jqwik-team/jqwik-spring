package net.jqwik.spring;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.assertj.core.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;

@AddLifecycleHook(JqwikSpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SimpleSpringTests {

	@Autowired
	MyBean myBean;

	@Property(tries = 5)
	void accessAutowiredBean() {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
	}

	@Property(tries = 5)
	void accessInjectedBean(@Autowired MyBean injected) {
		Assertions.assertThat(injected.sayHello()).isEqualTo("hello");
	}

	@Property(tries = 5)
	@AddLifecycleHook(EnsureDifferentPrototypeInstances.class)
	void accessBeanWithPrototypeScope(@Autowired PrototypeBean prototype) {
		System.out.println("Prototype = " + prototype.hashCode());
	}

	private static class EnsureDifferentPrototypeInstances implements AroundTryHook, AroundPropertyHook {

		@Override
		public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property) {
			Store<Set<PrototypeBean>> instances = Store.create("instances", Lifespan.PROPERTY, HashSet::new);
			try {
				return property.execute();
			} finally {
				Assertions.assertThat(instances.get()).hasSize(5);
			}
		}

		@Override
		public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters) {
			Store<Set<PrototypeBean>> instances = Store.get("instances");
			instances.update(set -> {
				set.add((PrototypeBean) parameters.get(0));
				return set;
			});
			return aTry.execute(parameters);
		}
	}
}
