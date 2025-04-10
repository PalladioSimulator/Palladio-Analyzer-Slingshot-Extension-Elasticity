package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter;

import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.ui.SPDModelConfiguration;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.ui.SPDModelProvider;
import org.palladiosimulator.analyzer.slingshot.core.extension.AbstractSlingshotExtension;
import org.palladiosimulator.elasticity.ElasticitySpec;

public class SPDInterpreterModule extends AbstractSlingshotExtension {

    @Override
    protected void configure() {
        install(SPDModelConfiguration.class);
        install(SpdBehavior.class);
        install(RepeatedSimulationTimeReachedRepeater.class);
        provideModel(ElasticitySpec.class, SPDModelProvider.class);
    }

}
