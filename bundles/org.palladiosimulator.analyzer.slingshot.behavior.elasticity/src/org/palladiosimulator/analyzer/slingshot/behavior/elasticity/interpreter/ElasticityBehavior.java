package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter;

import static org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.EventCardinality.MANY;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.data.SimulationTimeReached;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.data.SpdBasedEvent;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.SpdInterpreter.InterpretationResult;
import org.palladiosimulator.analyzer.slingshot.common.annotations.Nullable;
import org.palladiosimulator.analyzer.slingshot.core.api.SimulationDriver;
import org.palladiosimulator.analyzer.slingshot.core.events.PreSimulationConfigurationStarted;
import org.palladiosimulator.analyzer.slingshot.core.extension.SimulationBehaviorExtension;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.Subscribe;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.OnEvent;
import org.palladiosimulator.analyzer.slingshot.eventdriver.returntypes.Result;
import org.palladiosimulator.elasticity.ElasticitySpec;

/**
 * The behavior where the interpretation of SPD starts. The interpreter might return new events that
 * could be of the following kind:
 *
 * <ul>
 * <li>Events that are directly scheduled at a certain time, such as {@link SimulationTimeReached}
 * </ul>
 *
 * @author Julijan Katic
 */
@OnEvent(when = PreSimulationConfigurationStarted.class, then = SpdBasedEvent.class, cardinality = MANY)
public class SpdBehavior implements SimulationBehaviorExtension {

    private static final Logger LOGGER = Logger.getLogger(SpdBehavior.class);

    private final SimulationDriver driver;
    private final ElasticitySpec elasticitySpecModel;

    @Inject
    public SpdBehavior(final SimulationDriver driver, @Nullable final ElasticitySpec elasticitySpecModel) {
        this.elasticitySpecModel = elasticitySpecModel;
        this.driver = driver;
    }

    @Override
    public boolean isActive() {
        return this.elasticitySpecModel != null;
    }

    @Subscribe
    public Result<SpdBasedEvent> onPreSimulationConfigurationStarted(
            final PreSimulationConfigurationStarted configurationStarted) {
        final SpdInterpreter interpreter = new SpdInterpreter();
        final InterpretationResult result = interpreter.doSwitch(this.elasticitySpecModel);

        LOGGER.debug("The result of the SPD interpretation is not null: " + (result != null));

        result.getAdjustorContexts()
            .stream()
            .peek(ac -> LOGGER.debug("AdjustorContext: #handlers = " + ac.getAssociatedHandlers()
                .size()))
            .flatMap(ac -> ac.getAssociatedHandlers()
                .stream())
            .forEach(driver::registerEventHandler);

        return Result.from(result.getEventsToSchedule());
    }
}
