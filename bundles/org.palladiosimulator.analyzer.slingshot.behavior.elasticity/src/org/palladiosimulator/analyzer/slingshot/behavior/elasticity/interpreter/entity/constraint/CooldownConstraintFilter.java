package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entity.constraint;

import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.ElasticitySpecAdjustorState;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.FilterObjectWrapper;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.FilterResult;
import org.palladiosimulator.elasticity.constraints.policy.CooldownConstraint;

/**
 * The CooldownConstraint filter evaluates whether the time in which an adjustment 
 * could potentially occur is exceeding the specified cooldown. 
 * 
 * In case the time is within the cooldown then the filter yields success when 
 * the max number of adjustments within the cooldown has not yet been reached.
 * 
 * The state upon which the evaluation occurs is in {@link ElasticitySpecAdjustorState}. 
 * The state is altered upon a successful adjustment in the {@link Adjustor} filter. 
 * 
 * @author Julijan Katic, Floriment Klinaku
 *
 */
public class CooldownConstraintFilter extends AbstractConstraintFilter<CooldownConstraint> {

	public CooldownConstraintFilter(final CooldownConstraint constraint) {
		super(constraint);
	}

	@Override
	public FilterResult doProcess(final FilterObjectWrapper event) {
		final int numberScalesInCooldown = event.getState().getNumberOfScalesInCooldown();
		final double cooldownEnd = event.getState().getCoolDownEnd();
		
		if(event.getEventToFilter().time()>=cooldownEnd) {
			return FilterResult.success(event.getEventToFilter());
		} else {
			if (numberScalesInCooldown < constraint.getMaxScalingOperations()) {
				return FilterResult.success(event.getEventToFilter());
			} else {
				return FilterResult.disregard(String.format("Max number scales reached: %d >= %d", numberScalesInCooldown, constraint.getMaxScalingOperations()));
			}
		}
	}

}
