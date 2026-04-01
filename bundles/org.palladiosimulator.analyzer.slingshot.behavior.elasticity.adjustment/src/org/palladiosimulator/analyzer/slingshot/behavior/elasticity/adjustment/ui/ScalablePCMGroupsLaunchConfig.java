package org.palladiosimulator.analyzer.slingshot.behavior.elasticity.adjustment.ui;

import org.palladiosimulator.analyzer.slingshot.core.extension.SystemBehaviorExtension;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.Subscribe;
import org.palladiosimulator.analyzer.slingshot.eventdriver.annotations.eventcontract.OnEvent;
import org.palladiosimulator.analyzer.slingshot.ui.events.ArchitectureModelsTabBuilderStarted;
import org.palladiosimulator.analyzer.slingshot.workflow.events.WorkflowLaunchConfigurationBuilderInitialized;
import org.palladiosimulator.scalablepcmgroups.ScalablePCMGroups;

@OnEvent(when = ArchitectureModelsTabBuilderStarted.class)
@OnEvent(when = WorkflowLaunchConfigurationBuilderInitialized.class)
public class ScalablePCMGroupsLaunchConfig implements SystemBehaviorExtension {

    private static final String FILE_NAME = "scalablepcmgroups";

    @Subscribe
    public void onArchitectureModelsTab(final ArchitectureModelsTabBuilderStarted tab) {
        tab.newModelDefinition()
            .fileName(FILE_NAME)
            .modelClass(ScalablePCMGroups.class)
            .label("Scalable PCM Groups Configuration")
            .optional(true)
            .build();
    }

    @Subscribe
    public void onWorkflowConfiguration(final WorkflowLaunchConfigurationBuilderInitialized init) {
        init.getConfiguration(FILE_NAME, "scalablepcmgroups", (conf, model) -> conf.addOtherModelFile((String) model));
    }

}
