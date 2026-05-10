package com.jongsoft.finance.configuration.conditions;

import com.jongsoft.finance.core.adapter.api.ModuleProvider;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.AnnotationValue;

import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleEnabledCondition implements Condition {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ModuleEnabledCondition.class);

    private final Map<String, Boolean> cachedChecks = new ConcurrentHashMap<>();

    @Override
    public boolean matches(ConditionContext context) {
        String requiredModule = context.getComponent()
                .findAnnotation(Requires.class)
                .filter(r -> r.get("condition", Condition.class)
                        .filter(ModuleEnabledCondition.class::isInstance)
                        .isPresent())
                .map(AnnotationValue::stringValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);
        if (requiredModule == null) {
            logger.info("No module required.");
            return true;
        }

        return cachedChecks.computeIfAbsent(
                requiredModule, _ -> lookupModuleStatus(context, requiredModule));
    }

    private boolean lookupModuleStatus(ConditionContext<?> context, String module) {
        ModuleProvider moduleProvider = context.getBean(ModuleProvider.class);
        if (moduleProvider == null) {
            logger.error("Cannot find the module provider system, fatal exception.");
            context.fail("Module provider system not found");
            return true;
        }

        boolean isEnabled = moduleProvider.isModuleEnabled(module);
        logger.info("Checking if module {} is enabled, result {}.", module, isEnabled);
        return isEnabled;
    }
}
