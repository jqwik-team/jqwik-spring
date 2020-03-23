package net.jqwik.spring;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SimpleSpringJupiterTests {

	@Autowired
	MyBean myBean;

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
		System.out.println("Prototype 1 = " + prototype.hashCode());
	}

	@Test
	void accessBeanWithPrototypeScope2(@Autowired PrototypeBean prototype) {
		System.out.println("Prototype 2 = " + prototype.hashCode());
	}

}
