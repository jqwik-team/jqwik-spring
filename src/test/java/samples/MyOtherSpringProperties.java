package samples;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;

@JqwikSpringSupport
@ContextConfiguration(classes = MySpringConfig.class)
class MyOtherSpringProperties {
	@Autowired
	MyOtherSpringProperties(MySpringBean springBean) {
		Assertions.assertNotNull(springBean);
	}

	@BeforeProperty
	void beforeProperty(@Autowired MySpringBean springBean) {
		Assertions.assertNotNull(springBean);
	}

	@Property
	void beanIsInjected(@Autowired MySpringBean springBean) {
		Assertions.assertNotNull(springBean);
	}
}
