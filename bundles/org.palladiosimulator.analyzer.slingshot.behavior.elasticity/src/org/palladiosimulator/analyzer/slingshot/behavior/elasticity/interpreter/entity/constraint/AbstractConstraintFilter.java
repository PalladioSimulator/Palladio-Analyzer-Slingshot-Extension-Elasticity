package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entity.constraint;

import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.Filter;
import org.palladiosimulator.elasticity.constraints.AbstractConstraint;
import org.palladiosimulator.elasticity.constraints.policy.CooldownConstraint;
import org.palladiosimulator.elasticity.constraints.policy.IntervalConstraint;
import org.palladiosimulator.elasticity.constraints.policy.PolicyConstraint;
import org.palladiosimulator.elasticity.constraints.target.TargetConstraint;
import org.palladiosimulator.elasticity.constraints.target.ThrashingConstraint;

/**
 * Defines accordingly to {@link PolicyConstraint} or {@link TargetConstraint} whether the
 * constraint is met and a trigger can happen or not.
 *
 * @author Julijan Katic
 */
public abstract class AbstractConstraintFilter<T extends AbstractConstraint> implements Filter {

    protected final T constraint;

    public AbstractConstraintFilter(final T constraint) {
        this.constraint = constraint;
    }

    public static Filter createAbstractConstraintFilter(final AbstractConstraint constraint) {
        if (constraint instanceof final CooldownConstraint cooldownConstraint) {
            return new CooldownConstraintFilter(cooldownConstraint);
        } else if (constraint instanceof final IntervalConstraint intervallConstraint) {
            return new IntervalConstraintFilter(intervallConstraint);
        } else if (constraint instanceof final ThrashingConstraint thrashingConstraint) {
            return new ThrashingConstraintFilter(thrashingConstraint);
        } else {
            throw new UnsupportedOperationException(
                    "Currently, only cooldown, intervall and thrashing constraints are supported");
        }
    }
}
