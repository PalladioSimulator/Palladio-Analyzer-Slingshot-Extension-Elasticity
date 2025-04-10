package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities;

public abstract class ComboundFilter extends FilterChain implements Filter {

    public ComboundFilter(ElasticitySpecAdjustorState state) {
        super(state);
    }

    /**
     * Constructor without the Elasticity Spec adjustor state.
     * 
     * A compound filter typically doesn't need a state on its own, but instead the child filter's
     * need to hold them.
     */
    public ComboundFilter() {
        super(null);
    }

}
