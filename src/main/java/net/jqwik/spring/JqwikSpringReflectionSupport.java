package net.jqwik.spring;

import java.lang.reflect.*;
import java.util.*;

/**
 * This should somehow move to jqwik itself and be provided as a service to hooks
 */
class JqwikSpringReflectionSupport {

	interface Applier {
		void apply(Object instance) throws Exception;
	}

	static void applyToInstances(Object instance, Applier codeToApply) throws Exception {
		List<Object> instances = getInstances(instance);
		for (Object i : instances) {
			codeToApply.apply(i);
		}
	}

	private static List<Object> getInstances(Object instance) {
		return getInstances(instance, new ArrayList<>());
	}

	private static List<Object> getInstances(Object inner, List<Object> innerInstances) {
		Optional<Object> optionalOuter = getOuterInstance(inner);
		innerInstances.add(0, inner);
		return optionalOuter.map(outer -> getInstances(outer, innerInstances))
							.orElse(innerInstances);
	}

	// Copied from JqwikReflectionSupport
	private static Optional<Object> getOuterInstance(Object inner) {
		// This is risky since it depends on the name of the field which is nowhere guaranteed
		// but has been stable so far in all JDKs

		return Arrays
				   .stream(inner.getClass().getDeclaredFields())
				   .filter(field -> field.getName().startsWith("this$"))
				   .findFirst()
				   .map(field -> {
					   try {
						   return makeAccessible(field).get(inner);
					   } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
						   return Optional.empty();
					   }
				   });
	}

	// Copied from JqwikReflectionSupport
	private static <T extends AccessibleObject> T makeAccessible(T object) {
		if (!object.isAccessible()) {
			object.setAccessible(true);
		}
		return object;
	}
}
