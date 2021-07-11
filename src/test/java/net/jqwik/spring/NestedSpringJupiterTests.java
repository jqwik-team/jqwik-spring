package net.jqwik.spring;

import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

import net.jqwik.api.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class NestedSpringJupiterTests {

	@Autowired
	public NestedSpringJupiterTests(@Qualifier("TestName") String testName) {
		Assertions.assertThat(testName).isEqualTo("The Test Name");
	}

	@Autowired
	MyBean myBean;

	@Mock
	List<String> mockList;

	@Nested
		// Annotation not present below Spring Boot 2.4.0
		// @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.INHERIT)
	class NestedTests {
		@Test
		void accessAutowiredBean() {
			Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
		}

		@Test
		void accessInjectedBean(@Autowired MyBean injected) {
			Assertions.assertThat(injected.sayHello()).isEqualTo("hello");
		}

		@Test
		void accessBeanWithPrototypeScope1(@Autowired PrototypeBean prototype) {
			Assertions.assertThat(prototype).isNotNull();
			//System.out.println("Prototype 1 = " + prototype.hashCode());
		}

		@Test
		void accessBeanWithPrototypeScope2(@Autowired PrototypeBean prototype) {
			Assertions.assertThat(prototype).isNotNull();
			//System.out.println("Prototype 2 = " + prototype.hashCode());
		}

		@Test
		void mocksAreInitialized() {
			Assertions.assertThat(mockList).hasSize(0);
		}

	}
}
