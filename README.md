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

## How to Install

### Gradle

Follow the 
[instructions here](https://jqwik.net/docs/current/user-guide.html#gradle)
and add the following dependency to your `build.gradle` file:

```
dependencies {
    ...
    testImplementation("net.jqwik:jqwik-spring:0.5.0")
}
```

### Maven

Follow the 
[instructions here](https://jqwik.net/docs/current/user-guide.html#maven)
and add the following dependency to your `pom.xml` file:

```
<dependency>
  <groupId>net.jqwik</groupId>
  <artifactId>jqwik-spring</artifactId>
  <version>0.5.0</version>
  <scope>test</scope>
</dependency>
```


## Standard Usage

To enable autowiring of a Spring application context or beans you just have to
add `@AddLifecycleHook(JqwikSpringExtension.class)` to your test container class:

```java
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;

@AddLifecycleHook(JqwikSpringExtension.class)
@ContextConfiguration(classes = MySpringConfig.class)
public class MySpringProperties {

  @Autowired
  MySpringBean mySpringBean;

  @Property
  void nameIsAddedToHello(@ForAll @AlphaChars @StringLength(min = 1) String name) {
    String greeting = mySpringBean.sayHello(name);
    Assertions.assertTrue(greeting.contains(name));
  }
}
```

Configuration and autowiring of values is delegated to Spring's own test framework. Therefore all 
[integration testing annotations](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-spring)
can be used. This is also true for 
[standard annotation support](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-standard)

### Lifecycle

Spring will recreate its application context for each annotated class.
That means, that

- Singleton beans will only be created once for all tests of one test container class. 
- Properties and tries within the same class _share mutual state_ of all Spring-controlled beans. 

If you want a property to recreate the app context for each try, you have to annotate
the property method with 
[`@DirtiesContext`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#spring-testing-annotation-dirtiescontext). 
Compare the following two properties:

```java
@AddLifecycleHook(JqwikSpringExtension.class)
@ContextConfiguration(classes = MySpringConfig.class)
public class MySpringProperties {

  @Property(tries = 10)
  void counterIsCountingUp(@Autowired MyCounter counter) {
    counter.inc();
    // Prints out 1, 2, 3 ... 10
    System.out.println(counter.value());
  }

  @Property(tries = 10)
  @DirtiesContext
  void counterIsAlways1(@Autowired MyCounter counter) {
    counter.inc();
    // Prints out 1, 1, 1 ... 1
    System.out.println(counter.value());
  }
}
```

### Parameter injection

### Annotations

There are two dedicated annotations to simplify set up of jqwik test container
classes with Spring:

- `@SpringJqwikConfig`: Works just like
  [`@SpringJunitConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitconfig),
  but for jqwik properties.

- `@SpringJqwikWebConfig`: Works just like
  [`@SpringJunitWebConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitwebconfig),
  but for jqwik properties.

### Spring JUnit Jupiter Testing Annotations

_jqwik_'s Spring support is trying to mostly simulate how Spring's native
Jupiter support works. Therefore, some of that stuff also works, but a few things do not.

#### Supported Jupiter Test Annotations

- [`@TestConstructor`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-testconstructor)

#### Unsupported Jupiter Test Annotations

- [`@SpringJunitConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitconfig): Replace with `@net.jqwik.spring.SpringJqwikConfig`
 
- [`@SpringJunitWebConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitwebconfig): Replace with `@net.jqwik.spring.SpringJqwikWebConfig` 

- `@EnabledIf`: Planned for future versions

- `@DisabledIf`: Planned for future versions

## Spring Boot

The current version has no dedicated support for Spring Boot but the usual stuff
works out of the box.

## Shortcomings

The Spring extension and configuration is _NOT_ handed down to inner test groups.
Also, member variables in the outer instance are not being auto wired
even if the inner class has all necessary annotations.
This is due to limitations of Spring's own testing framework and cannot be fixed
by this library.



