# jqwik Spring Support

This project provides an extension to support testing of Spring and Spring-Boot applications with [jqwik](https://jqwik.net).


## Installation

## Spring TestContext Framework

To enable autowiring of a Spring application context or beans you just have to
add `@AddLifecycleHook(JqwikSpringExtension.class)` to your test container class.

### Supported Spring Test Annotations

- `@ContextConfiguration`

- `@WebAppConfiguration`

- `@TestConstructor`

### Unsupported Stuff

- `@SpringJUnitConfig`: Replace with `@SpringJqwikConfig`
 
- `@SpringJUnitWebConfig`: Replace with `@SpringJqwikWebConfig` 

## Spring Boot

The current version has no dedicated support for Spring Boot but the usual stuff
works out of the box.

## Links

[Spring Testing](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html)

[`@SpringJunitConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitconfig)

[`@SpringJunitWebConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitwebconfig)