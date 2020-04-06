package net.jqwik.spring;

import net.jqwik.api.lifecycle.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.test.context.junit.jupiter.*;

class DisabledIfHook implements SkipExecutionHook {

	@Override
	public SkipResult shouldBeSkipped(LifecycleContext context) {
		if (!context.findAnnotation(DisabledIf.class).isPresent()) {
			return SkipResult.doNotSkip();
		}
		ExtensionContext extensionContext = new JupiterExtensionContextAdapter(context);
		ConditionEvaluationResult evaluationResult = new DisabledIfCondition().evaluateExecutionCondition(extensionContext);
		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse(null));
		}
		return SkipResult.doNotSkip();
	}

}
