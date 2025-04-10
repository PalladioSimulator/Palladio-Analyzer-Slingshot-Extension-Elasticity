package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment;

import javax.inject.Named;

import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment.qvto.QVToLoader;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment.qvto.QVToModelTransformation;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment.qvto.QVToReconfigurator;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment.ui.SemanticModelLaunchConfig;
import org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment.ui.SemanticModelProvider;
import org.palladiosimulator.analyzer.slingshot.core.extension.AbstractSlingshotExtension;
import org.palladiosimulator.semanticelasticityspec.Configuration;

import com.google.inject.Provides;

public class ElasticityAdjustorModule extends AbstractSlingshotExtension {

	private static final String MAIN_QVTO_FILE = "platform:/plugin/org.palladiosimulator.elasticity.semantic.transformations/transformations/elasticity/MainTransformation.qvto";
	public static final String MAIN_QVTO = "mainqvto";
	
	@Override
	protected void configure() {
		install(ElasticityAdjustmentBehavior.class);
		install(SemanticModelLaunchConfig.class);
		provideModel(Configuration.class, SemanticModelProvider.class);

		bind(QVToReconfigurator.class);
	}

	@Provides
	@Named(MAIN_QVTO)
	public String mainQvtoFile() {
		return MAIN_QVTO_FILE;
	}
	
	@Provides
	@Named(MAIN_QVTO)
	public Iterable<QVToModelTransformation> getTransformations() {
		return QVToLoader.loadFromFiles(MAIN_QVTO_FILE);
	}
	
}
