package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.data.ElasticityBasedEvent;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.Filter;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.LogicalANDComboundFilter;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.LogicalORCompoundFilter;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.entities.LogicalXORCompoundFilter;
import org.palladiosimulator.analyzer.slingshot.common.events.DESEvent;
import org.palladiosimulator.analyzer.slingshot.eventdriver.entity.Subscriber;
import org.palladiosimulator.elasticity.ScalingPolicy;
import org.palladiosimulator.elasticity.triggers.ComposedTrigger;
import org.palladiosimulator.elasticity.triggers.LogicalOperator;
import org.palladiosimulator.elasticity.triggers.SimpleFireOnTrend;
import org.palladiosimulator.elasticity.triggers.SimpleFireOnValue;
import org.palladiosimulator.elasticity.triggers.util.TriggersSwitch;

public class ScalingTriggerInterpreter extends TriggersSwitch<ScalingTriggerInterpreter.InterpretationResult> {

    final ScalingPolicy policy;

    public ScalingTriggerInterpreter(final ScalingPolicy policy) {
        super();
        this.policy = policy;
    }

    @Override
    public InterpretationResult caseComposedTrigger(final ComposedTrigger object) {
        return object.getScalingtrigger()
            .stream()
            .map(this::doSwitch)
            .reduce((res1, res2) -> res1.addFrom(res2, object.getLogicalOperator()))
            .orElseGet(InterpretationResult::new);
    }

    @Override
    public InterpretationResult caseSimpleFireOnValue(final SimpleFireOnValue object) {
        final StimuliInterpreter stimuliInterpreter = new StimuliInterpreter(this, object);
        return stimuliInterpreter.doSwitch(object.getStimulus());
    }

    @Override
    public InterpretationResult caseSimpleFireOnTrend(final SimpleFireOnTrend object) {
        // TODO Auto-generated method stub
        return super.caseSimpleFireOnTrend(object);
    }

    static final class InterpretationResult {

        private Filter triggerChecker;
        private final List<ElasticityBasedEvent> eventsToSchedule = new ArrayList<>();
        private final List<Subscriber.Builder<? extends DESEvent>> eventsToListen = new ArrayList<>();

        public InterpretationResult triggerChecker(final Filter triggerChecker) {
            this.triggerChecker = triggerChecker;
            return this;
        }

        public InterpretationResult scheduleEvent(final ElasticityBasedEvent event) {
            this.eventsToSchedule.add(event);
            return this;
        }

        public InterpretationResult listenEvent(final Subscriber.Builder<? extends DESEvent> event) {
            this.eventsToListen.add(event);
            return this;
        }

        public Filter getTriggerChecker() {
            return this.triggerChecker;
        }

        public List<ElasticityBasedEvent> getEventsToSchedule() {
            return this.eventsToSchedule;
        }

        public List<Subscriber.Builder<? extends DESEvent>> getEventsToListen() {
            return this.eventsToListen;
        }

        // TODO: Please look at code style again..
        public InterpretationResult addFrom(final InterpretationResult other, final LogicalOperator operator) {
            this.eventsToSchedule.addAll(other.eventsToSchedule);
            this.eventsToListen.addAll(other.eventsToListen);

            if (this.triggerChecker == null) {
                /* We then simply set as the other */
                this.triggerChecker = other.triggerChecker;
            } else {
                switch (operator) {
                case AND: {
                    final Filter temp = this.triggerChecker;
                    if (temp instanceof final LogicalANDComboundFilter chain) {
                        chain.add(other.triggerChecker);
                    } else {
                        final LogicalANDComboundFilter comboundFilter = new LogicalANDComboundFilter();
                        comboundFilter.add(temp);
                        comboundFilter.add(other.triggerChecker);
                        this.triggerChecker = comboundFilter;
                    }
                }
                    break;
                case OR: {
                    final LogicalORCompoundFilter comboundFilter = new LogicalORCompoundFilter();
                    comboundFilter.add(this.triggerChecker);
                    comboundFilter.add(other.triggerChecker);
                    this.triggerChecker = comboundFilter;
                }
                    break;
                case XOR: {
                    final LogicalXORCompoundFilter comboundFilter = new LogicalXORCompoundFilter();
                    comboundFilter.add(this.triggerChecker);
                    comboundFilter.add(other.triggerChecker);
                    this.triggerChecker = comboundFilter;
                }
                    break;
                default:
                    break;
                }
            }

            return this;
        }
    }
}
