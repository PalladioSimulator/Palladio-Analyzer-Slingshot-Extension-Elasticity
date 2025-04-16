package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entity.targetgroup;

import java.util.Objects;

import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.data.RepeatedSimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.FilterResult;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.utils.MeasuringPointInsideTargetGroup;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.monitor.data.events.MeasurementMade;
import org.palladiosimulator.elasticity.targets.TargetGroup;
import org.palladiosimulator.scalablepcmgroupmeasuringpoint.InfrastructureGroupMeasuringPoint;

public class TargetGroupChecker implements Filter {

    private final TargetGroup targetGroup;
    private final MeasuringPointInsideTargetGroup measuringPointInsideTargetGroupSwitch;

    public TargetGroupChecker(final TargetGroup targetGroup) {
        this.targetGroup = Objects.requireNonNull(targetGroup);
        this.measuringPointInsideTargetGroupSwitch = new MeasuringPointInsideTargetGroup(targetGroup);
    }

    @Override
    public FilterResult doProcess(final FilterObjectWrapper objectWrapper) {
        final DESEvent event = objectWrapper.getEventToFilter();
        if (event instanceof final MeasurementMade mm) {
            if (mm.getEntity()
                .getMeasuringPoint() instanceof InfrastructureGroupMeasuringPoint) {
                return FilterResult.success(event);
            }
            if (this.measuringPointInsideTargetGroupSwitch.doSwitch(mm.getEntity()
                .getMeasuringPoint())) {
                return FilterResult.success(event);
            }
            return FilterResult.disregard("The measurement is not inside this target group");
        }
        final SimulationTimeReached simulationTimeReached;
        if (event instanceof RepeatedSimulationTimeReached repeatedSimulationTimeReached) {
            simulationTimeReached = new SimulationTimeReached(repeatedSimulationTimeReached.getTargetGroupId(),
                    repeatedSimulationTimeReached.time(), repeatedSimulationTimeReached.delay());
        } else if ((event instanceof SimulationTimeReached || event instanceof RepeatedSimulationTimeReached)) {
            simulationTimeReached = (SimulationTimeReached) event;
        } else {
            return FilterResult.disregard(
                    "The event can only be checked if it is a MeasurementMade OR SimulationTimeReached at the moment.");
        }
        if (simulationTimeReached.getTargetGroupId()
            .equals(targetGroup.getId())) {
            return FilterResult.success(event);
        }
        return FilterResult.disregard("The target group does not match the event.");
    }
}
