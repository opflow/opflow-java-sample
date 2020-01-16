package com.devebot.opflow.sample.bdd;

import com.devebot.opflow.sample.bdd.steps.FibonacciClientSteps;
import com.devebot.opflow.sample.bdd.steps.FibonacciServerSteps;
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
public class FibonacciServerStories extends FibonacciEmbedder {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(),
                new FibonacciClientSteps(),
                new FibonacciServerSteps());
    }
     
    @Override
    protected List<String> storyPaths() {
        String codeLocation = CodeLocations.codeLocationFromClass(this.getClass()).getFile();
        return new StoryFinder().findPaths(codeLocation, Arrays.asList("**/server*.story"), Arrays.asList(""), "file:" + codeLocation);
    }
}
