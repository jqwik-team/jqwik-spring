package net.jqwik.spring;

import net.jqwik.api.lifecycle.*;
import org.apiguardian.api.*;

/**
 * This class includes all the jqwik hooks necessary to use spring in examples and properties
 */
@API(status = API.Status.EXPERIMENTAL, since = "0.1.0")
public class JqwikSpringExtension implements LifecycleHook {

	@Override
	public PropagationMode propagateTo() {
		return PropagationMode.DIRECT_DESCENDANTS;
	}
}
