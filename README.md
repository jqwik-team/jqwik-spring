# jqwik Spring Support

This project provides an extension to support testing of Spring and Spring-Boot applications with [jqwik](https://jqwik.net).

<!-- use `doctoc --maxlevel 3 README.md` to recreate the TOC -->
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
### Table of Contents  

- [Installation](#installation)
- [Spring TestContext Framework](#spring-testcontext-framework)
  - [Lifecycle](#lifecycle)
  - [Annotations](#annotations)
  - [Supported Spring Test Annotations](#supported-spring-test-annotations)
  - [Unsupported Stuff](#unsupported-stuff)
- [Spring Boot](#spring-boot)
- [Links](#links)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Installation

## Spring TestContext Framework

To enable autowiring of a Spring application context or beans you just have to
add `@AddLifecycleHook(JqwikSpringExtension.class)` to your test container class.

### Lifecycle

### Annotations

There are two dedicated annotations to simplify set up of jqwik test container
classes with Spring:

- `@SpringJqwikConfig`

- `@SpringJqwikWebConfig`

### Supported Spring Test Annotations

- `@ContextConfiguration`

- `@WebAppConfiguration`

- `@TestConstructor`

### Unsupported Stuff

- `@SpringJUnitConfig`: Replace with `@SpringJqwikConfig`
 
- `@SpringJUnitWebConfig`: Replace with `@SpringJqwikWebConfig` 

- `@EnabledIf`: Planned for future versions

- `@DisabledIf`: Planned for future versions

## Spring Boot

The current version has no dedicated support for Spring Boot but the usual stuff
works out of the box.

## Links

[Spring Testing](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html)

[`@SpringJunitConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitconfig)

[`@SpringJunitWebConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitwebconfig)