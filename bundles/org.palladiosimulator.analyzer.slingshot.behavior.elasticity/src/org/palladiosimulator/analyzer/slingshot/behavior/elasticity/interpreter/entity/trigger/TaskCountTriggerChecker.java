package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entity.trigger;

import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;

import org.palladiosimulator.analyzer.slingshot.monitor.data.entities.SlingshotMeasuringValue;
import org.palladiosimulator.elasticity.targets.TargetGroup;
import org.palladiosimulator.elasticity.triggers.BaseTrigger;
import org.palladiosimulator.elasticity.triggers.expectations.ExpectedCount;
import org.palladiosimulator.elasticity.triggers.stimuli.TaskCount;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

public class TaskCountTriggerChecker extends AbstractManagedElementTriggerChecker<TaskCount> {

    public TaskCountTriggerChecker(final BaseTrigger trigger, final TaskCount stimulus, final TargetGroup targetGroup) {
        super(trigger, stimulus, targetGroup, Set.of(ExpectedCount.class),
                MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE,
                MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC);
    }

    /* We need to retrieve the correct type (Long) instead of Double */
    @Override
    protected double getValueForAggregation(final SlingshotMeasuringValue smv) {
        final Measure<Long, Dimensionless> measure = smv.getMeasureForMetric(this.baseMetricDescription);
        final long value = measure.getValue();
        return value;
    }
}
