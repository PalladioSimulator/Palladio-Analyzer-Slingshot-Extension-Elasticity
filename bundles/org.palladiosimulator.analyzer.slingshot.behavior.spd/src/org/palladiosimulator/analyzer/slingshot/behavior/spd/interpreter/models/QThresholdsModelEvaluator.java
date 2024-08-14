package org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.models;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.analyzer.slingshot.behavior.spd.interpreter.entity.aggregator.ModelAggregatorWrapper;
import org.palladiosimulator.spd.adjustments.models.QThresholdsModel;

public class QThresholdsModelEvaluator extends LearningBasedModelEvaluator {

    public QThresholdsModelEvaluator(QThresholdsModel model, List<ModelAggregatorWrapper<?>> stimuliListeners) {
        super(stimuliListeners);
    }

    @Override
    public int getDecision() throws Exception {
        List<Double> input = new ArrayList<>();
        for (ModelAggregatorWrapper<?> modelAggregatorWrapper : aggregatorList) {
            input.add(modelAggregatorWrapper.getResult());
        }
        // TODO IMPORTANT Add model evaluation here
        return 0;
    }
}
