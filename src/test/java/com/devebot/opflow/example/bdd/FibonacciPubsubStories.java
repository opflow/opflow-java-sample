package com.devebot.opflow.example.bdd;

import com.devebot.opflow.example.bdd.steps.FibonacciPubsubSteps;
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
public class FibonacciPubsubStories extends FibonacciEmbedder {
    
    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(),
                new FibonacciPubsubSteps());
    }
    
    @Override
    protected List<String> storyPaths() {
        String codeLocation = CodeLocations.codeLocationFromClass(this.getClass()).getFile();
        return new StoryFinder().findPaths(codeLocation, Arrays.asList("**/pubsub_*.story"), Arrays.asList(""), "file:" + codeLocation);
    }
}
