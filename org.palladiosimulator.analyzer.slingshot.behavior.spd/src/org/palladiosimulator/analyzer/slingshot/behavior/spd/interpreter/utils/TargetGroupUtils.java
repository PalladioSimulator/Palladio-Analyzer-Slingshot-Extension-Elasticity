package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.utils;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.spd.targets.ElasticInfrastructure;

public class TargetGroupUtils {
	
	public static boolean isContainerInElasticInfrastructure(final ResourceContainer container, final ElasticInfrastructure targetGroup) {
		return targetGroup.getPCM_ResourceEnvironment()
						  .getResourceContainer_ResourceEnvironment()
						  .stream()
						  .anyMatch(rc -> rc.getId().equals(container.getId()));
	}
	
}
