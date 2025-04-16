package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment.qvto.QVToModelTransformation;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment.qvto.QVToReconfigurator;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.data.ModelAdjustmentRequested;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.monitor.ResourceContainerMonitorCloner;
import org.palladiosimulator.analyzer.slingshot.common.annotations.Nullable;
import org.palladiosimulator.analyzer.slingshot.common.events.modelchanges.AllocationChange;
import org.palladiosimulator.analyzer.slingshot.common.events.modelchanges.ModelAdjusted;
import org.palladiosimulator.analyzer.slingshot.common.events.modelchanges.ModelChange;
import org.palladiosimulator.analyzer.slingshot.common.events.modelchanges.MonitorChange;
import org.palladiosimulator.analyzer.slingshot.common.events.modelchanges.ResourceEnvironmentChange;
import org.palladiosimulator.analyzer.slingshot.core.extension.SimulationBehaviorExtension;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.Subscribe;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.EventCardinality;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.OnEvent;
import org.palladiosimulator.analyzer.slingshot.eventdriver.returntypes.Result;
import org.palladiosimulator.elasticity.ElasticitySpec;
import org.palladiosimulator.elasticity.ModelBasedScalingPolicy;
import org.palladiosimulator.monitorrepository.MonitorRepository;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.scalablepcmgroups.InfrastructureGroup;
import org.palladiosimulator.scalablepcmgroups.ScalablePCMGroups;
import org.palladiosimulator.scalablepcmgroups.scalablepcmgroupsFactory;

@OnEvent(when = ModelAdjustmentRequested.class, then = ModelAdjusted.class, cardinality = EventCardinality.SINGLE)
public class ElasticityAdjustmentBehavior implements SimulationBehaviorExtension {

    private static final Logger LOGGER = Logger.getLogger(ElasticityAdjustmentBehavior.class);

    private final boolean activated;

    private final ElasticitySpec elasticitySpec;
    private final QVToReconfigurator reconfigurator;
    private final Iterable<QVToModelTransformation> transformations;
    private final Allocation allocation;
    private final ScalablePCMGroups scalablePCMGroups;
    private final MonitorRepository monitorRepository;

    @Inject
    public ElasticityAdjustmentBehavior(final Allocation allocation,
            final @Nullable MonitorRepository monitorRepository, final @Nullable ScalablePCMGroups scalablePCMGroups,
            final @Nullable ElasticitySpec elasticitySpec, final QVToReconfigurator reconfigurator,
            @Named(ElasticityAdjustorModule.MAIN_QVTO) final Iterable<QVToModelTransformation> transformations) {
        this.activated = monitorRepository != null && scalablePCMGroups != null && elasticitySpec != null;
        this.allocation = allocation;
        this.scalablePCMGroups = scalablePCMGroups;
        this.elasticitySpec = elasticitySpec;
        this.reconfigurator = reconfigurator;
        this.transformations = transformations;
        this.monitorRepository = monitorRepository;
    }

    @Override
    public boolean isActive() {
        return this.activated;
    }

    @Subscribe
    public Result<ModelAdjusted> onModelAdjustmentRequested(final ModelAdjustmentRequested event) {
        final ResourceEnvironment environment = allocation.getTargetResourceEnvironment_Allocation();

        /*
         * Since the model is provided by the user, the model will be available in the cache
         * already. A special case is the predictive trigger that supports both scaling up and down
         * with varying magnitude. It changes the scaling policy, thus a new model will need to be
         * cached
         */
        if (event.getScalingPolicy() instanceof ModelBasedScalingPolicy) {
            final ScalablePCMGroups scalablePCMGroups = this.scalablePCMGroups;
            scalablePCMGroups.setEnactedPolicy(event.getScalingPolicy());
            this.reconfigurator.getModelCache()
                .storeModel(scalablePCMGroups);
        }
        // Set the enacted policy for the next transformation
        this.scalablePCMGroups.setEnactedPolicy(event.getScalingPolicy());
        final List<ResourceContainer> oldContainers = new ArrayList<>(
                environment.getResourceContainer_ResourceEnvironment());
        final List<AllocationContext> oldAllocationContexts = new ArrayList<>(
                allocation.getAllocationContexts_Allocation());

        final boolean result = this.reconfigurator.execute(this.transformations);

        LOGGER.debug("RECONFIGURATION WAS " + result);

        if (result) {
            LOGGER
                .debug("Number of resource container is now: " + environment.getResourceContainer_ResourceEnvironment()
                    .size());

            /*
             * Calculate what the new and deleted resource containers are for tracking.
             */
            final List<ResourceContainer> newResourceContainers = new ArrayList<>(
                    environment.getResourceContainer_ResourceEnvironment());
            newResourceContainers.removeAll(oldContainers);

            final List<ResourceContainer> deletedResourceContainers = new ArrayList<>(oldContainers);
            deletedResourceContainers.removeAll(environment.getResourceContainer_ResourceEnvironment());

            final List<AllocationContext> newAllocationContexts = new ArrayList<>(
                    allocation.getAllocationContexts_Allocation());
            newAllocationContexts.removeAll(oldAllocationContexts);

            final List<ModelChange<?>> changes = new ArrayList<>();

            changes.add(ResourceEnvironmentChange.builder()
                .resourceEnvironment(environment)
                .simulationTime(event.time())
                .oldResourceContainers(oldContainers)
                .newResourceContainers(newResourceContainers)
                .deletedResourceContainers(deletedResourceContainers)
                .build());

            changes.add(AllocationChange.builder()
                .allocation(allocation)
                .newAllocationContexts(newAllocationContexts)
                .build());

            changes.addAll(this.createMonitors(newResourceContainers, event.time()));

            return Result.of(new ModelAdjusted(true, changes));
        } else {
            return Result.of(new ModelAdjusted(false, Collections.emptyList()));
        }

    }

    /**
     * Create new monitors for all new containers created by the reconfiguration transformation.
     *
     * The news monitors match the monitors defined for the original container.
     *
     * @param newContainers
     *            containers created by the reconfiguration transformation
     * @param simulationTime
     *            time of the reconfiguration
     * @return List of newly created monitors for all resource containers in {@code newContainers}.
     */
    private List<MonitorChange> createMonitors(final List<ResourceContainer> newContainers,
            final double simulationTime) {
        if (newContainers.isEmpty() || this.getUnitContainer(newContainers.get(0)) == null) {
            return Collections.emptyList();
        }

        final ResourceContainer unitContainer = getUnitContainer(newContainers.get(0));

        final ResourceContainerMonitorCloner cloner = new ResourceContainerMonitorCloner(this.monitorRepository,
                monitorRepository.getMonitors()
                    .get(0)
                    .getMeasuringPoint()
                    .getMeasuringPointRepository(),
                unitContainer);

        return newContainers.stream()
            .flatMap(container -> cloner.createMonitorsForResourceContainer(container)
                .stream())
            .map(newMonitor -> new MonitorChange(newMonitor, null, simulationTime))
            .toList();
    }

    /**
     * Get the resource container which {@code referenceContainer} is a replica of.
     *
     * I.e. get the {@code unit} of of the {@link ElasticInfrastructureCfg} that has
     * {@code referenceContainer} in its elements.
     *
     * @param referenceContainer
     *            a replicated resource container, must not be null.
     * @return Unit resource container which {@code referenceContainer} is a replica of.
     */
    private ResourceContainer getUnitContainer(final ResourceContainer referenceContainer) {
        assert referenceContainer != null : "Reference Container is null but must not be null.";

        return this.scalablePCMGroups.getTargetCfgs()
            .stream()
            .filter(InfrastructureGroup.class::isInstance)
            .map(InfrastructureGroup.class::cast)
            .filter(eicfg -> eicfg.getElements()
                .contains(referenceContainer))
            .map(el -> el.getUnit())
            .findAny()
            .orElse(null);
    }

    /*
     * We leave the following methods for now, as we will need to make the Configuration through a
     * dedicated launch tab instead.
     */

    private InfrastructureGroup createElasticInfrastructureCfg(final ResourceEnvironment environment) {
        final InfrastructureGroup targetGroupConfig = scalablepcmgroupsFactory.eINSTANCE.createInfrastructureGroup();
        targetGroupConfig.setResourceEnvironment(environment);
        targetGroupConfig.setUnit(environment.getResourceContainer_ResourceEnvironment()
            .stream()
            .findAny()
            .get());
        targetGroupConfig.getElements()
            .addAll(environment.getResourceContainer_ResourceEnvironment());
        return targetGroupConfig;
    }

    /**
     * Helper method for creating the {@link Configuration}
     */
    private ScalablePCMGroups createConfiguration(final ModelAdjustmentRequested event,
            final ResourceEnvironment environment) {
        final ScalablePCMGroups scalablePCMGroups = scalablepcmgroupsFactory.eINSTANCE.createScalablePCMGroups();
        scalablePCMGroups.setEnactedPolicy(event.getScalingPolicy());

        final InfrastructureGroup targetGroupConfig = createElasticInfrastructureCfg(environment);
        scalablePCMGroups.getTargetCfgs()
            .add(targetGroupConfig);

        return scalablePCMGroups;
    }

}
