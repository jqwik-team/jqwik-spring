package net.jqwik.spring.annotations;

import net.jqwik.api.*;
import net.jqwik.spring.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.test.context.*;

import static org.assertj.core.api.Assertions.*;

@SpringJqwikConfig(TestConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TestConstructorTests {

	public TestConstructorTests(MyBean myBean) {
		this.myBean = myBean;
	}

	MyBean myBean;

	@Property(tries = 3)
	void useAutowiredMember() {
		assertThat(myBean.sayHello()).isEqualTo("hello");
	}
}
