package net.jqwik.spring.annotations;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.assertj.core.api.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

@JqwikSpringSupport
@TestPropertySource(properties = {"mytest.disabled = true"})
@ContextConfiguration(classes = TestConfig.class)
class DisabledIfWithJqwikTests {

	private static final List<String> run = new ArrayList<>();

	@AfterContainer
	static void checkRunMethods() {
		Assertions.assertThat(run).containsExactlyInAnyOrder(
				"shouldBeEnabled",
				"shouldBeEnabledBySpringEL"
		);
	}

	@Example
	@DisabledIf(
			expression = "false"
	)
	void shouldBeEnabled() {
		run.add("shouldBeEnabled");
	}

	@Example
	@DisabledIf(expression = "${mytest.disabled}", loadContext = true)
	void shouldBeDisabledByPropertyValue() {
		run.add("shouldBeDisabledByPropertyValue");
	}

	@Example
	@DisabledIf("#{42 == 41}")
	void shouldBeEnabledBySpringEL() {
		run.add("shouldBeEnabledBySpringEL");
	}

	@Example
	@DisabledIf(expression = "true")
	void shouldBeDisabled() {
		run.add("shouldBeDisabled");
	}

	@JqwikSpringSupport
	@DisabledIf("true")
	@ContextConfiguration(classes = TestConfig.class)
	static class ShouldBeDisabledTests {

		@Example
		void shouldNotBeCalled() {
			Assertions.fail("should be disabled by DisabledIf annotation on class");
		}
	}
}
