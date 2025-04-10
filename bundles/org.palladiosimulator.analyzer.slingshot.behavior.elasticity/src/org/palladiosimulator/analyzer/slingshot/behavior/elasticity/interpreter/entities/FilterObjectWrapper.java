package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities;

import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;

/**
 * This class wraps the object to filter with the associated
 * state of the adjustment, so that each filter can adjust the
 * state and retrieve it.
 * 
 * @author Julijan Katic
 *
 */
public final class FilterObjectWrapper {

	private final DESEvent eventToFilter;
	private final ElasticitySpecAdjustorState state;
	
	public FilterObjectWrapper(DESEvent objectToFilter, ElasticitySpecAdjustorState state) {
		super();
		this.eventToFilter = objectToFilter;
		this.state = state;
	}
	
	public DESEvent getEventToFilter() {
		return eventToFilter;
	}
	
	public ElasticitySpecAdjustorState getState() {
		return state;
	}
	
	
}
