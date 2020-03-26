package samples;

import org.springframework.context.annotation.*;

@Configuration
public class MySpringConfig {

	@Bean
	MySpringBean mySpringBean() {
		return new MySpringBean();
	}

	@Bean
	MyCounter myCounter() {
		return new MyCounter();
	}
}
