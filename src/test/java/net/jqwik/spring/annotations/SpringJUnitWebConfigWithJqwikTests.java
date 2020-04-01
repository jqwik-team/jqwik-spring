package net.jqwik.spring.annotations;

import net.jqwik.api.*;
import net.jqwik.spring.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit.jupiter.web.*;
import org.springframework.web.context.*;

import static org.assertj.core.api.Assertions.*;

@JqwikSpringSupport
@SpringJUnitWebConfig(TestConfig.class)
class SpringJUnitWebConfigWithJqwikTests {

	@Autowired
	MyBean myBean;

	@Property(tries = 3)
	void useAutowiredMember() {
		assertThat(myBean.sayHello()).isEqualTo("hello");
	}

	@Autowired
	private WebApplicationContext webAppContext;

	@Example
	void useWebAppContext() {
		assertThat(webAppContext).isNotNull();
	}
}
