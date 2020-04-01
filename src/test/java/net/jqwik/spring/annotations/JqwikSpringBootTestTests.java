package net.jqwik.spring.annotations;

import net.jqwik.api.*;
import net.jqwik.spring.boot.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.*;
import org.springframework.test.web.servlet.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@JqwikSpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.MOCK,
		classes = TestableSpringBootApplication.class
)
@AutoConfigureMockMvc
class JqwikSpringBootTestTests {

	@Autowired
	private MockMvc mvc;

	@Example
	void helloWorldWorks() throws Exception {
		mvc.perform(get("/hello").contentType(MediaType.APPLICATION_JSON))
		   .andExpect(status().isOk())
		   .andExpect(content().string("world"));
	}
}
