package net.jqwik.spring;

import org.springframework.context.annotation.*;

@Configuration
public class TestConfig {

	@Bean
	MyBean myBean() {
		return new MyBean();
	}
}
