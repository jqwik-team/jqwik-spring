package samples;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.*;

@AddLifecycleHook(JqwikSpringExtension.class)
@ContextConfiguration(classes = MySpringConfig.class)
public class MySpringProperties {

	@Autowired
	MySpringBean mySpringBean;

	@Property
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
	@AddLifecycleHook(JqwikSpringExtension.class)
	@ContextConfiguration(classes = MySpringConfig.class)
	class NestedProperties {

		@Example
		void nestedExample(@Autowired MySpringBean bean) {
			// this.mySpringBean is not being autowired!
			String greeting = bean.sayHello("test");
			Assertions.assertTrue(greeting.contains("test"));
		}

	}
}
