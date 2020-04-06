package net.jqwik.spring;

import net.jqwik.api.lifecycle.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.test.context.junit.jupiter.*;

class EnabledIfHook implements SkipExecutionHook {

	@Override
	public SkipResult shouldBeSkipped(LifecycleContext context) {
		if (!context.findAnnotation(EnabledIf.class).isPresent()) {
			return SkipResult.doNotSkip();
		}
		ExtensionContext extensionContext = new JupiterExtensionContextAdapter(context);
		ConditionEvaluationResult evaluationResult = new EnabledIfCondition().evaluateExecutionCondition(extensionContext);
		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse(null));
		}
		return SkipResult.doNotSkip();
	}

}
