# jqwik Spring Support

This project provides an extension to support testing of Spring and Spring-Boot applications with [jqwik](https://jqwik.net).

<!-- use `doctoc --maxlevel 3 README.md` to recreate the TOC -->
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
### Table of Contents  

- [How to Install](#how-to-install)
  - [Gradle](#gradle)
  - [Maven](#maven)
  - [Supported Spring Versions](#supported-spring-versions)
  - [Supported JUnit Platform Versions](#supported-junit-platform-versions)
- [Standard Usage](#standard-usage)
  - [Lifecycle](#lifecycle)
  - [Parameter Resolution of Autowired Beans](#parameter-resolution-of-autowired-beans)
  - [Spring JUnit Jupiter Testing Annotations](#spring-junit-jupiter-testing-annotations)
- [Spring Boot](#spring-boot)
- [Shortcomings](#shortcomings)
  - [Nested/Grouped Tests in Old Spring (Boot) Versions](#nestedgrouped-tests-in-old-spring-boot-versions)
- [Release Notes](#release-notes)
  - [0.9.0](#090)
  - [0.8.2](#082)
  - [0.8.1](#081)
  - [0.8.0](#080)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## How to Install

### Gradle

Follow the 
[instructions here](https://jqwik.net/docs/current/user-guide.html#gradle)
and add the following dependency to your `build.gradle` file:

```
dependencies {
    implementation("org.springframework:spring-context:5.3.14")
    ...
    testImplementation("net.jqwik:jqwik-spring:0.9.0")
    testImplementation("org.springframework:spring-test:5.3.14")
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
  <version>5.3.14</version>
</dependency>
...
<dependency>
  <groupId>net.jqwik</groupId>
  <artifactId>jqwik-spring</artifactId>
  <version>0.9.0</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-test</artifactId>
  <version>5.3.14</version>
  <scope>test</scope>
</dependency>
```

### Supported Spring Versions

You have to provide your own version of Spring or Spring Boot through
Gradle or Maven. The _jqwik-spring_ library has been tested with versions:

- `5.2.15-RELEASE`
- `5.3.14`

Please report any compatibility issues you stumble upon.

### Supported JUnit Platform Versions

You need at least version `1.8.2` of the JUnit platform - otherwise 
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

- [`@TestConstructor`](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-testconstructor)

- [`@SpringJunitConfig`](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-junit-jupiter-springjunitconfig)
 
- [`@SpringJunitWebConfig`](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-junit-jupiter-springjunitwebconfig)

- [`@EnabledIf`](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-junit-jupiter-enabledif)

- [`@DisabledIf`](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-junit-jupiter-disabledif)

- [`@NestedTestConfiguration`](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-nestedtestconfiguration)

## Spring Boot

By using `@JqwikSpringSupport` as described above most - if not all - Spring Boot
testing features, e.g. [test auto-configuration annotations](https://docs.spring.io/spring-boot/docs/current/reference/html/test-auto-configuration.html#test-auto-configuration) should work.

This was tested with the following Spring Boot versions:

- `2.2.13.RELEASE`
- `2.3.12.RELEASE`
- `2.4.8`
- `2.5.8`
- `2.6.2`

Please report any issues you have with other versions.

## Shortcomings

### Nested/Grouped Tests in Old Spring (Boot) Versions 

Up to Spring version `5.2.15.RELEASE`, which comes with Spring Boot `2.3.12.RELEASE`,
the Spring extension and configuration is _NOT_ handed down to inner test groups.
Also, member variables in the outer instance are not being auto wired
even if the inner class has all necessary annotations.
This is due to [limitations of Spring's own testing framework](https://github.com/spring-projects/spring-framework/issues/19930) 
and cannot be fixed by this library.



## Release Notes

### 0.9.0

- Upgrade jqwik 1.6.3
- Upgrade to JUnitPlatform 5.8.2
- Tested with Spring 5.3.14
- Tested with Spring Boot 2.6.2

### 0.8.2

- Upgrade jqwik 1.5.6
- Upgrade to JUnitPlatform 5.8.1
- Tested with Spring 5.3.11
- Tested with Spring Boot 2.5.5

### 0.8.1

- For Spring >= 5.3.0 test configuration is now "inherited" to
  nested container classes annotated with `@Group`.
  
### 0.8.0

- Upgrade to jqwik 1.5.3
- Tested with more recent versions of Spring and Spring Boot