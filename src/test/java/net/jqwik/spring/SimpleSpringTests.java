package net.jqwik.spring;

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
//		System.out.println(">>>>>>>>>>>>>>>>>>>> " + myBean);
		Assertions.assertThat(myBean.sayHello()).isEqualTo("hello");
	}
}
