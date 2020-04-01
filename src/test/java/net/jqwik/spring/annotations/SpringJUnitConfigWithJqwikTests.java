package net.jqwik.spring.annotations;

import net.jqwik.api.*;
import net.jqwik.spring.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.test.context.junit.jupiter.*;

import static org.assertj.core.api.Assertions.*;

@JqwikSpringSupport
@SpringJUnitConfig(TestConfig.class)
class SpringJUnitConfigWithJqwikTests {

	@Autowired
	MyBean myBean;

	@Autowired
	private ApplicationContext applicationContext;

	@Property(tries = 3)
	void useAutowiredMember() {
		assertThat(myBean.sayHello()).isEqualTo("hello");
	}

	@Example
	void useAppContext() {
		assertThat(applicationContext).isNotNull();
	}
}
