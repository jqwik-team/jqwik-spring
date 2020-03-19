package net.jqwik.spring;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import org.assertj.core.api.*;
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
}
