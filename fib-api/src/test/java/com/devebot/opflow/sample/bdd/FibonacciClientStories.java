package com.devebot.opflow.sample.bdd;

import com.devebot.opflow.sample.bdd.steps.FibonacciClientSteps;
import java.util.Arrays;
import java.util.List;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciClientStories extends FibonacciEmbedder {
    
    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(),
                new FibonacciClientSteps());
    }
     
    @Override
    protected List<String> storyPaths() {
        String codeLocation = CodeLocations.codeLocationFromClass(this.getClass()).getFile();
        return new StoryFinder().findPaths(codeLocation, Arrays.asList("**/client*.story"), Arrays.asList(""), "file:" + codeLocation);
    }
}
