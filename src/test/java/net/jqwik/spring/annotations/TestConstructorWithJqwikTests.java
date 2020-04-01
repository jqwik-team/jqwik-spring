package net.jqwik.spring.annotations;

import net.jqwik.api.*;
import net.jqwik.spring.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

import static org.assertj.core.api.Assertions.*;

@JqwikSpringSupport
@SpringJUnitConfig(TestConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TestConstructorWithJqwikTests {

	TestConstructorWithJqwikTests(MyBean myBean) {
		this.myBean = myBean;
	}

	MyBean myBean;

	@Property(tries = 3)
	void useAutowiredMember() {
		assertThat(myBean.sayHello()).isEqualTo("hello");
	}
}
