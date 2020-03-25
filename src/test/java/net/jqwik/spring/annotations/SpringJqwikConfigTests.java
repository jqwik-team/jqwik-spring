package net.jqwik.spring.annotations;

import net.jqwik.api.*;
import net.jqwik.spring.*;
import org.assertj.core.api.*;
import org.springframework.beans.factory.annotation.*;

@SpringJqwikConfig(TestConfig.class)
class SpringJqwikConfigTests {

	@Autowired
	MyBean myBean;

	@Property(tries = 3)
	void useAutowiredMember() {
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
	}
}
