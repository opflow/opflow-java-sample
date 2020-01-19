package com.devebot.opflow.sample.bdd;

import com.devebot.opflow.sample.bdd.steps.FibonacciMasterSteps;
import com.devebot.opflow.sample.bdd.steps.FibonacciWorkerSteps;
import java.util.Arrays;
import java.util.List;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciWorkerStories extends FibonacciEmbedder {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(),
                new FibonacciMasterSteps(),
                new FibonacciWorkerSteps());
    }
     
    @Override
    protected List<String> storyPaths() {
        String codeLocation = CodeLocations.codeLocationFromClass(this.getClass()).getFile();
        return new StoryFinder().findPaths(codeLocation, Arrays.asList("**/worker*.story"), Arrays.asList(""), "file:" + codeLocation);
    }
}
