package samples;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.spring.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MySpringConfig.class)
class MySpringJUnitTests {

	@Autowired
	private MySpringBean mySpringBean;

	@Test
	@Property(tries = 10)
	void counterIsCountingUp(@Autowired MyCounter counter) {
		counter.inc();
		System.out.println(counter.value());
	}

	@Test
	@DirtiesContext
	void counterIsAlways1(@Autowired MyCounter counter) {
		counter.inc();
		System.out.println(counter.value());
	}

	@Nested
	@NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.INHERIT)
	class NestedTests {

		@Test
		void nestedExample(@Autowired MySpringBean injectedBean) {
			String greeting = mySpringBean.sayHello("test");
			Assertions.assertTrue(greeting.contains("test"));

			String greetingInjected = injectedBean.sayHello("test");
			Assertions.assertTrue(greetingInjected.contains("test"));
		}

	}
}
