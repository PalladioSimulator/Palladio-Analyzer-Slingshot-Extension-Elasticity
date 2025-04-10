package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entity.trigger;

import java.util.Set;

import org.palladiosimulator.elasticity.targets.TargetGroup;
import org.palladiosimulator.elasticity.triggers.BaseTrigger;
import org.palladiosimulator.elasticity.triggers.expectations.ExpectedPercentage;
import org.palladiosimulator.elasticity.triggers.stimuli.CPUUtilization;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

public class CPUUtilizationTriggerChecker extends AbstractManagedElementTriggerChecker<CPUUtilization> {

    public CPUUtilizationTriggerChecker(final BaseTrigger trigger, final CPUUtilization stimulus,
            final TargetGroup targetGroup) {
        super(trigger, stimulus, targetGroup, Set.of(ExpectedPercentage.class),
                MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
    }

}
