package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter;

import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.ui.ElasticitySpecModelConfiguration;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.ui.ElasticitySpecModelProvider;
import org.palladiosimulator.analyzer.slingshot.core.extension.AbstractSlingshotExtension;
import org.palladiosimulator.elasticity.ElasticitySpec;

public class ElasticitySpecInterpreterModule extends AbstractSlingshotExtension {

    @Override
    protected void configure() {
        install(ElasticitySpecModelConfiguration.class);
        install(ElasticityBehavior.class);
        install(RepeatedSimulationTimeReachedRepeater.class);
        provideModel(ElasticitySpec.class, ElasticitySpecModelProvider.class);
    }

}
