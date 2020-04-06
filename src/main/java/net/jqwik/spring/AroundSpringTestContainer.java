package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.lifecycle.*;

class AroundSpringTestContainer implements BeforeContainerHook, AfterContainerHook {

	@Override
	public boolean appliesTo(Optional<AnnotatedElement> optionalElement) {
		// Only apply to container classes
		return optionalElement.map(element -> element instanceof Class).orElse(false);
	}

	@Override
	public void beforeContainer(ContainerLifecycleContext context) throws Exception {
		Optional<Class<?>> optionalContainerClass = context.optionalContainerClass();
		if (optionalContainerClass.isPresent()) {
			JqwikSpringExtension.getTestContextManager(optionalContainerClass.get()).beforeTestClass();
		}
	}

	@Override
	public void afterContainer(ContainerLifecycleContext context) throws Exception {
		Optional<Class<?>> optionalContainerClass = context.optionalContainerClass();
		if (optionalContainerClass.isPresent()) {
			JqwikSpringExtension.getTestContextManager(optionalContainerClass.get()).afterTestClass();
		}
	}

	@Override
	public int beforeContainerProximity() {
		return -20;
	}

}
