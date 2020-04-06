package net.jqwik.spring.annotations;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.assertj.core.api.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

@JqwikSpringSupport
@TestPropertySource(properties = {"mytest.enabled = true"})
@ContextConfiguration(classes = TestConfig.class)
class EnabledIfWithJqwikTests {

	private static final List<String> run = new ArrayList<>();

	@AfterContainer
	static void checkRunMethods() {
		Assertions.assertThat(run).containsExactlyInAnyOrder(
				"shouldBeEnabled",
				"shouldBeEnabledByPropertyValue",
				"shouldBeEnabledBySpringEL"
		);
	}

	@Example
	@EnabledIf(
			expression = "true",
			reason = "Enabled"
	)
	void shouldBeEnabled() {
		run.add("shouldBeEnabled");
	}

	@Example
	@EnabledIf(expression = "${mytest.enabled}", loadContext = true)
	void shouldBeEnabledByPropertyValue() {
		run.add("shouldBeEnabledByPropertyValue");
	}

	@Example
	@EnabledIf("#{42 != 41}")
	void shouldBeEnabledBySpringEL() {
		run.add("shouldBeEnabledBySpringEL");
	}

	@Example
	@EnabledIf(
			expression = "false",
			reason = "Enabled"
	)
	void shouldBeDisabled() {
		run.add("shouldBeDisabled");
	}

}
