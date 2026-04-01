package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.interpreter.ui;

import org.palladiosimulator.analyzer.slingshot.core.extension.SystemBehaviorExtension;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.Subscribe;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.OnEvent;
import org.palladiosimulator.analyzer.slingshot.ui.events.ArchitectureModelsTabBuilderStarted;
import org.palladiosimulator.analyzer.slingshot.workflow.events.WorkflowLaunchConfigurationBuilderInitialized;
import org.palladiosimulator.elasticity.ElasticitySpec;

@OnEvent(when = ArchitectureModelsTabBuilderStarted.class)
@OnEvent(when = WorkflowLaunchConfigurationBuilderInitialized.class)
public class ElasticitySpecModelConfiguration implements SystemBehaviorExtension {

    public static final String FILE_NAME = "elasticity";
    public static final String[] FILE_EXTENSIONS = new String[] { "*.elasticity" };

    @Subscribe
    public void onArchitectureModelsTab(final ArchitectureModelsTabBuilderStarted event) {
        event.newModelDefinition()
            .fileName(FILE_NAME)
            .fileExtensions(FILE_EXTENSIONS)
            .modelClass(ElasticitySpec.class)
            .label("Elasticity Specification")
            .optional(true)
            .build();
    }

    @Subscribe
    public void onWorkflowConfigurationInitialized(final WorkflowLaunchConfigurationBuilderInitialized event) {
        event.getConfiguration(FILE_NAME, "", (conf, modelFile) -> conf.addOtherModelFile((String) modelFile));
    }

}
