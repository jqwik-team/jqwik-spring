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
    implementation("org.springframework:spring-context:5.2.5.RELEASE")
    ...
    testImplementation("net.jqwik:jqwik-spring:0.7.0")
    testImplementation("org.springframework:spring-test:5.2.5.RELEASE")
}
```

You can look at a 
[sample project](https://github.com/jlink/jqwik-samples/tree/master/jqwik-spring-boot-gradle)
 using jqwik, Spring Boot and Gradle.

### Maven

Follow the 
[instructions here](https://jqwik.net/docs/current/user-guide.html#maven)
and add the following dependency to your `pom.xml` file:

```
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-context</artifactId>
  <version>5.2.5.RELEASE</version>
</dependency>
...
<dependency>
  <groupId>net.jqwik</groupId>
  <artifactId>jqwik-spring</artifactId>
  <version>0.7.0</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-test</artifactId>
  <version>5.2.5.RELEASE</version>
  <scope>test</scope>
</dependency>
```

### Supported Spring Versions

You have to provide your own version of Spring or Spring Boot through
Gradle or Maven. The _jqwik-spring_ library has been tested with versions:

- `5.2.0-RELEASE`
- `5.2.4-RELEASE`
- `5.2.5-RELEASE`

Please report any compatibility issues you stumble upon.

### Supported JUnit Platform Versions

You need at least version `1.6.2` of the JUnit platform - otherwise 
strange things _could_ happen.
Keep in mind that if you are using Spring Boot you will have to 
[explicitly set the JUnit platform version](https://stackoverflow.com/a/54605523/32352).

## Standard Usage

To enable autowiring of a Spring application context or beans you just have to
add `@JqwikSpringSupport` to your test container class:

```java
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.spring.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;

@JqwikSpringSupport
@ContextConfiguration(classes = MySpringConfig.class)
class MySpringProperties {

  @Autowired
  private MySpringBean mySpringBean;

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
[standard annotation support](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-standard).

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
@JqwikSpringSupport
@ContextConfiguration(classes = MySpringConfig.class)
class MySpringProperties {

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

### Parameter Resolution of Autowired Beans 

Autowired beans will be injected as parameters in example and property methods,
in all 
[lifecycle methods](https://jqwik.net/docs/current/user-guide.html#annotated-lifecycle-methods)
and also in the test container class's constructor - if there is only one:

```java
@JqwikSpringSupport
@ContextConfiguration(classes = MySpringConfig.class)
class MyOtherSpringProperties {
    @Autowired
    MyOtherSpringProperties(MySpringBean springBean) {
        Assertions.assertNotNull(springBean);
    }

    @BeforeProperty
    void beforeProperty(@Autowired MySpringBean springBean) {
        Assertions.assertNotNull(springBean);
    }

    @Property
    void beanIsInjected(@Autowired MySpringBean springBean) {
        Assertions.assertNotNull(springBean);
    }
}
```

### Spring JUnit Jupiter Testing Annotations

_jqwik_'s Spring support is trying to mostly simulate how Spring's native
Jupiter support works. Therefore, some of that stuff also works, but a few things do not.

#### Supported Jupiter Test Annotations

- [`@TestConstructor`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-testconstructor)

- [`@SpringJunitConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitconfig)
 
- [`@SpringJunitWebConfig`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-springjunitwebconfig)

- [`@EnabledIf`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-enabledif)

- [`@DisabledIf`](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#integration-testing-annotations-junit-jupiter-disabledif)

## Spring Boot

By using `@JqwikSpringSupport` as described above most - if not all - Spring Boot
testing features, e.g. [test auto-configuration annotations](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/html/appendix-test-auto-configuration.html#test-auto-configuration) should work.

This was tested with the following Spring Boot versions:

- `2.2.0.RELEASE`
- `2.2.5.RELEASE`
- `2.2.6.RELEASE`


## Shortcomings

The Spring extension and configuration is _NOT_ handed down to inner test groups.
Also, member variables in the outer instance are not being auto wired
even if the inner class has all necessary annotations.
This is due to limitations of Spring's own testing framework and cannot be fixed
by this library.



