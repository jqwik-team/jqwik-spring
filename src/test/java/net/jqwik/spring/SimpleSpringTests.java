package net.jqwik.spring;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.assertj.core.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;

@AddLifecycleHook(JqwikSpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SimpleSpringTests {

	@Autowired
	public SimpleSpringTests(@Qualifier("TestName") String testName) {
		Assertions.assertThat(testName).isEqualTo("The Test Name");
	}

	@Autowired
	MyBean myBean;

	@Mock
	List<String> mockList;

	@BeforeContainer
	static void beforeContainer(@Autowired MyBean myBean, @Autowired @Qualifier("TestName") String testName) {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
		Assertions.assertThat(testName).isEqualTo("The Test Name");
	}

	@AfterContainer
	static void afterContainer(@Autowired MyBean myBean, @Autowired @Qualifier("TestName") String testName) {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
		Assertions.assertThat(testName).isEqualTo("The Test Name");
	}

	@BeforeProperty
	void beforeProperty(@Autowired MyBean myBean, @Autowired @Qualifier("TestName") String testName) {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
		Assertions.assertThat(testName).isEqualTo("The Test Name");
	}

	@AfterProperty
	void afterProperty(@Autowired MyBean myBean, @Autowired @Qualifier("TestName") String testName) {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
		Assertions.assertThat(testName).isEqualTo("The Test Name");
	}

	@BeforeTry
	void beforeTry(@Autowired MyBean myBean, @Autowired @Qualifier("TestName") String testName) {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
		Assertions.assertThat(testName).isEqualTo("The Test Name");
	}

	@AfterTry
	void afterTry(@Autowired MyBean myBean, @Autowired @Qualifier("TestName") String testName) {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
		Assertions.assertThat(testName).isEqualTo("The Test Name");
	}

	@Property(tries = 5)
	void accessAutowiredBean() {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
	}

	@Property(tries = 5)
	void accessInjectedBean(@Autowired MyBean injected) {
		Assertions.assertThat(injected.sayHello()).isEqualTo("hello");
	}

	@Property(tries = 5)
	void mocksAreInitializedPerTry() {
		Assertions.assertThat(mockList).hasSize(0);
		Mockito.when(mockList.size()).thenReturn(42);
	}

	@Property(tries = 5)
	@AddLifecycleHook(EnsureDifferentPrototypeInstances.class)
	void accessBeanWithPrototypeScope(@Autowired PrototypeBean prototype) {
		Assertions.assertThat(prototype).isNotNull();
		// System.out.println("Prototype = " + prototype.hashCode());
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
