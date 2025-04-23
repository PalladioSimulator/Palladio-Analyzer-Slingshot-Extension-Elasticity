package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.utils;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.palladiosimulator.analyzer.slingshot.core.Slingshot;
import org.palladiosimulator.elasticity.targets.CompetingConsumersGroupTarget;
import org.palladiosimulator.elasticity.targets.InfrastructureGroupTarget;
import org.palladiosimulator.elasticity.targets.ServiceGroupTarget;
import org.palladiosimulator.elasticity.targets.TargetGroup;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.scalablepcmgroups.CompetingConsumersGroup;
import org.palladiosimulator.scalablepcmgroups.InfrastructureGroup;
import org.palladiosimulator.scalablepcmgroups.ScalablePCMGroups;
import org.palladiosimulator.scalablepcmgroups.ServiceGroup;

/**
 * Utility methods for checking whether some component are part of, or related to the target group.
 * They can be used to check whether the measurements from that component should be considered for
 * triggering a policy.
 * 
 * @author Julijan Katic
 */
public class TargetGroupUtils {

    private static final Allocation allocation = Slingshot.getInstance()
        .getInstance(Allocation.class);
    private static final ScalablePCMGroups scalablePCMGroups = Slingshot.getInstance()
        .getInstance(ScalablePCMGroups.class);

    /**
     * Checks whether the container is part of the infrastructure group.
     * 
     * @param container
     *            The resource container to check
     * @param targetGroup
     *            The infrastructure group referencing a resource environment.
     * @return true iff the container is part of the environment.
     */
    public static boolean isContainerInInfrastructureGroup(final ResourceContainer container,
            final InfrastructureGroup targetGroup) {

        List<InfrastructureGroup> infrastructureGroups = scalablePCMGroups.getTargetCfgs()
            .stream()
            .filter(cfg -> cfg instanceof InfrastructureGroup c)
            .map(c -> (InfrastructureGroup) c)
            .filter(c -> c.getUnit()
                .getId()
                .equals(targetGroup.getUnit()
                    .getId()))
            .collect(Collectors.toList());

        assert infrastructureGroups.size() == 1;

        return infrastructureGroups.get(0)
            .getElements()
            .stream()
            .anyMatch(rc -> rc.getId()
                .equals(container.getId()));

    }

    /**
     * Checks whether the container is part of the service group, by looking whether there exists an
     * assembly context (either the unit or a replicated) that is part of the service group and is
     * referencing the given container.
     * 
     * @param container
     *            The container to check.
     * @param serviceGroupTarget
     *            The service group to consider the assembly contexts from.
     * @return true if the container is part of the service group.
     * @see #isContainerInCompetingConsumersGroup(ResourceContainer, CompetingConsumersGroup)
     */
    public static boolean isContainerInServiceGroup(final ResourceContainer container,
            final ServiceGroup serviceGroup) {
        return isResourceContainerInContextsToConsider(container, () -> getAllContextsToConsider(serviceGroup));
    }

    /**
     * Very similar to {@link #isContainerInServiceGroup(ResourceContainer, ServiceGroup)}: It
     * checks whether the container is part of the competing consumer group, by checking whether
     * there exists an assembly context (either the unit or a replicated) that is part of the
     * competing consumer group and is referencing the given container.
     * 
     * @param container
     *            The container to check.
     * @param competingConsumersGroup
     *            The target group to consider the assembly contexts from.
     * @return true fi the container is part of the competing consumer group.
     */
    public static boolean isContainerInCompetingConsumersGroup(final ResourceContainer container,
            final CompetingConsumersGroup competingConsumersGroup) {
        return isResourceContainerInContextsToConsider(container,
                () -> getAllContextsToConsider(competingConsumersGroup));
    }

    /**
     * Checks whether the container is part of the target group. The way of how it is checked, and
     * what "being part of the target group" means, depends on the concrete type of the target
     * group.
     * 
     * @param container
     *            The container to check.
     * @param targetGroup
     *            The target group to consider.
     * @return true if the container is part of the target group.
     * 
     * @see #isContainerInCompetingConsumersGroup(ResourceContainer, CompetingConsumersGroup)
     * @see #isContainerInInfrastructureGroup(ResourceContainer, InfrastructureGroupTarget)
     * @see #isContainerInServiceGroup(ResourceContainer, ServiceGroup)
     */
    public static boolean isContainerInTargetGroup(final ResourceContainer container, final TargetGroup targetGroup) {
        if (targetGroup instanceof final InfrastructureGroupTarget infrastructureGroup) {
            return isContainerInInfrastructureGroup(container, infrastructureGroup.getInfrastructureGroup());
        }
        if (targetGroup instanceof final ServiceGroupTarget serviceGroupTarget) {
            return isContainerInServiceGroup(container, serviceGroupTarget.getServiceGroup());
        }
        if (targetGroup instanceof final CompetingConsumersGroupTarget competingConsumersGroupTarget) {
            return isContainerInCompetingConsumersGroup(container,
                    competingConsumersGroupTarget.getCompetingConsumersGroup());
        }

        return false;
    }

    /**
     * Checks whether the assembly context is part of the target group in the case if the target
     * group is of type {@link ServiceGroup} or {@link CompetingConsumersGroup}. The assembly
     * contexts to consider are the unit assemblies, as well as the replicated assemblies.
     * 
     * In case of the {@link org.palladiosimulator.elasticity.targets.InfrastructureGroup}, it
     * checks whether the context is referencing a resource container is part of the resource
     * environment referenced by the target group.
     * 
     * @param context
     *            The assembly context to check.
     * @param targetGroup
     *            The target group to consider.
     * @return True if the assembly context is part of the target group (or referneced by a
     *         container).
     */
    public static boolean isAssemblyInTargetGroup(final AssemblyContext context, final TargetGroup targetGroup) {
        if (targetGroup instanceof final ServiceGroup serviceGroup) {
            return getAllContextsToConsider(serviceGroup).anyMatch(ac -> ac.getId()
                .equals(context.getId()));
        }
        if (targetGroup instanceof final InfrastructureGroupTarget infrastructureGroup) {
            return infrastructureGroup.getInfrastructureGroup()
                .getResourceEnvironment()
                .getResourceContainer_ResourceEnvironment()
                .stream()
                .anyMatch(rc -> getContainerRelatedToContext(context).anyMatch(rcp -> rcp.getId()
                    .equals(rc.getId())));
        }
        if (targetGroup instanceof final CompetingConsumersGroup competingConsumers) {
            return getAllContextsToConsider(competingConsumers).anyMatch(ac -> ac.getId()
                .equals(context.getId()));
        }

        return false;
    }

    /**
     * Checks whether the operation signature is a provided role used by a component, that in turn
     * is referenced by an assembly contexts. Depending on the type of the target group, the
     * assembly contexts to consider are either the unit assembly contexts (or any of the replicated
     * ones) in case of {@link ServiceGroup} and {@link CompetingConsumersGroup}, or every assembly
     * context that is referencing a resource container in the
     * {@link org.palladiosimulator.elasticity.targets.InfrastructureGroup}.
     * 
     * @param operationSignature
     *            The signature to check.
     * @param targetGroup
     *            The target group to consider.
     * @return true if the operation signature is related to the target group.
     */
    public static boolean isOperationSinatureRelatedToTargetGroup(final OperationSignature operationSignature,
            final TargetGroup targetGroup) {
        if (targetGroup instanceof final CompetingConsumersGroup competingConsumers) {
            return anyContextHasSignature(getAllContextsToConsider(competingConsumers), operationSignature);
        }
        if (targetGroup instanceof final ServiceGroup serviceGroup) {
            return anyContextHasSignature(getAllContextsToConsider(serviceGroup), operationSignature);
        }
        if (targetGroup instanceof final InfrastructureGroupTarget infrastructure) {
            return anyContextHasSignature(getRelatedAssemblyContextFromInfrastructure(infrastructure),
                    operationSignature);
        }

        return false;
    }

    /**
     * Helper method for retrieving assembly contexts that reference any of the resource containers
     * in the Elastic Infrastructure.
     * 
     * @param infrastructureGroup
     *            The infrastructure of containers.
     * @return A stream of assembly context is reference
     */
    private static Stream<AssemblyContext> getRelatedAssemblyContextFromInfrastructure(
            final InfrastructureGroupTarget infrastructureGroup) {
        return infrastructureGroup.getInfrastructureGroup()
            .getResourceEnvironment()
            .getResourceContainer_ResourceEnvironment()
            .stream()
            .flatMap(rc -> allocation.getAllocationContexts_Allocation()
                .stream()
                .filter(ac -> ac.getResourceContainer_AllocationContext()
                    .getId()
                    .equals(rc.getId()))
                .map(AllocationContext::getAssemblyContext_AllocationContext));
    }

    /**
     * Checks whether any of the given assembly contexts has operation provided role that contains
     * the given signature.
     * 
     * @param assemblyContexts
     *            The stream of assembly contexts to consider.
     * @param operationSignature
     *            The signature to search for.
     * @return True if the siganture appears in any of the assembly contexts.
     */
    private static boolean anyContextHasSignature(final Stream<AssemblyContext> assemblyContexts,
            final OperationSignature operationSignature) {
        return assemblyContexts.map(AssemblyContext::getEncapsulatedComponent__AssemblyContext)
            .flatMap(rc -> rc.getProvidedRoles_InterfaceProvidingEntity()
                .stream())
            .filter(OperationProvidedRole.class::isInstance)
            .map(OperationProvidedRole.class::cast)
            .flatMap(opr -> opr.getProvidedInterface__OperationProvidedRole()
                .getSignatures__OperationInterface()
                .stream())
            .anyMatch(sig -> operationSignature.getId()
                .equals(sig.getId()));
    }

    /**
     * Helper method that returns all the resource containers that are related to the assembly
     * context through some allocation context.
     * 
     * @return stream of resource containers related to the assembly context.
     */
    private static Stream<ResourceContainer> getContainerRelatedToContext(final AssemblyContext context) {
        return allocation.getAllocationContexts_Allocation()
            .stream()
            .filter(ac -> ac.getAssemblyContext_AllocationContext()
                .getId()
                .equals(context.getId()))
            .map(AllocationContext::getResourceContainer_AllocationContext)
            .flatMap(rc -> rc.getResourceEnvironment_ResourceContainer()
                .getResourceContainer_ResourceEnvironment()
                .stream());
    }

    /**
     * Helper method to retrieve the assembly contexts that need to be considered when checking
     * whether the container is part of the target group. This includes the replicated contexts as
     * well.
     */
    private static Stream<AssemblyContext> getAllContextsToConsider(final ServiceGroup serviceGroup) {
        return scalablePCMGroups.getTargetCfgs()
            .stream()
            .filter(org.palladiosimulator.scalablepcmgroups.ServiceGroup.class::isInstance)
            .map(org.palladiosimulator.scalablepcmgroups.ServiceGroup.class::cast)
            .filter(sgc -> sgc.getUnit()
                .getId()
                .equals(serviceGroup.getUnit()
                    .getId()))
            .flatMap(sgc -> sgc.getElements()
                .stream());
    }

    private static Stream<AssemblyContext> getAllContextsToConsider(
            final CompetingConsumersGroup competingConsumersGroup) {
        return scalablePCMGroups.getTargetCfgs()
            .stream()
            .filter(org.palladiosimulator.scalablepcmgroups.CompetingConsumersGroup.class::isInstance)
            .map(org.palladiosimulator.scalablepcmgroups.CompetingConsumersGroup.class::cast)
            .filter(ccgc -> ccgc.getUnit()
                .getId()
                .equals(competingConsumersGroup.getUnit()
                    .getId()))
            .flatMap(ccgc -> Stream.concat(Stream.of(ccgc.getBrokerAssembly()), ccgc.getElements()
                .stream()));
    }

    /**
     * Checks whether the container is related by any of the assembly contexts given by the stream.
     * 
     * An assembly context is related to the resource container if there is an allocation context
     * referencing the assembly context and the resource container.
     */
    private static boolean isResourceContainerInContextsToConsider(final ResourceContainer container,
            final Supplier<Stream<AssemblyContext>> contextsToConsider) {
        return allocation.getAllocationContexts_Allocation()
            .stream()
            .filter(ac -> contextsToConsider.get()
                .anyMatch(asc -> ac.getAssemblyContext_AllocationContext()
                    .getId()
                    .equals(asc.getId())))
            .map(AllocationContext::getResourceContainer_AllocationContext)
            .anyMatch(rc -> rc.getId()
                .equals(container.getId()));
    }
}
