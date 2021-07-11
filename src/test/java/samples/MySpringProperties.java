package samples;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.*;

@JqwikSpringSupport
@ContextConfiguration(classes = MySpringConfig.class)
class MySpringProperties {

	@Autowired
	private MySpringBean mySpringBean;

	@Property(tries = 10)
	void nameIsAddedToHello(@ForAll @AlphaChars @StringLength(min = 1) String name) {
		String greeting = mySpringBean.sayHello(name);
		Assertions.assertTrue(greeting.contains(name));
	}

	@Property(tries = 10)
	void counterIsCountingUp(@Autowired MyCounter counter) {
		counter.inc();
		System.out.println(counter.value());
	}

	@Property(tries = 10)
	@DirtiesContext
	void counterIsAlways1(@Autowired MyCounter counter) {
		counter.inc();
		System.out.println(counter.value());
	}

	@Group
	class NestedProperties {

		@Example
		// This should work starting Spring Boot 2.4.0
		void nestedExample(@Autowired MySpringBean injectedBean) {
			String greeting = mySpringBean.sayHello("test");
			Assertions.assertTrue(greeting.contains("test"));

			String greetingInjected = injectedBean.sayHello("test");
			Assertions.assertTrue(greetingInjected.contains("test"));
		}

	}
}
