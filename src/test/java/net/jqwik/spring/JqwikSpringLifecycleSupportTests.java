package net.jqwik.spring;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.test.context.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;

import static org.assertj.core.api.Assertions.*;

@JqwikSpringSupport
@ContextConfiguration(classes = TestConfig.class)
class JqwikSpringLifecycleSupportTests {

	@Autowired
	MyBean myBean;

	@Example
	@AddLifecycleHook(MyHook.class)
	void contextIsAvailableInLifecycleHook() {
		assertThat(myBean).isNotNull();
	}

	static class MyHook implements AroundTryHook {
		@Override
		public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters) {
			Optional<ApplicationContext> applicationContext =
				JqwikSpringLifecycleSupport.applicationContext(context);
			assertThat(applicationContext).isPresent();
			assertThat(applicationContext.get().getBean(MyBean.class)).isNotNull();
			return aTry.execute(parameters);
		}
	}
}
