package net.jqwik.spring;

import org.springframework.beans.factory.config.*;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Scope;

@Configuration
public class TestConfig {

	@Bean
	MyBean myBean() {
		return new MyBean();
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	PrototypeBean prototypeBean() {
		return new PrototypeBean();
	}

	@Bean(name = "TestName")
	String testName() {
		return "The Test Name";
	}

}
