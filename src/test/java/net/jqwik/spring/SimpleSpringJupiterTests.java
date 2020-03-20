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
	void accessAutowiredBean1() {
//		System.out.println(">>>>>>>>>>>>>>>>>>>> " + myBean);
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
	}

	@Test
	void accessAutowiredBean2() {
//		System.out.println(">>>>>>>>>>>>>>>>>>>> " + myBean);
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
	}
}
