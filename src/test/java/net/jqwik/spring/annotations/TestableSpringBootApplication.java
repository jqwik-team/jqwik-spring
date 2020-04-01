package net.jqwik.spring.annotations;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class TestableSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestableSpringBootApplication.class, args);
	}

	@GetMapping("/hello")
	public String hello() {
		return "world";
	}
}
