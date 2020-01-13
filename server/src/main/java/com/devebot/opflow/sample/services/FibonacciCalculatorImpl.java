package com.devebot.opflow.sample.services;

import com.devebot.opflow.sample.business.FibonacciGenerator;
import com.devebot.opflow.sample.models.FibonacciInputItem;
import com.devebot.opflow.sample.models.FibonacciInputList;
import com.devebot.opflow.sample.models.FibonacciOutputItem;
import com.devebot.opflow.sample.models.FibonacciOutputList;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author drupalex
 */
public class FibonacciCalculatorImpl implements FibonacciCalculator {

    private final static Logger LOG = LoggerFactory.getLogger(FibonacciCalculatorImpl.class);

    @Override
    public FibonacciOutputItem calc(int number) {
        return new FibonacciGenerator(number).finish();
    }

    @Override
    public FibonacciOutputItem calc(FibonacciInputItem data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Request[{0}] - calc({1}) with number: {2}", 
                    new Object[] {
                        data.getRequestId(), FibonacciInputItem.class.getCanonicalName(), data.getNumber()
                    }));
        }
        return this.calc(data.getNumber());
    }

    @Override
    public FibonacciOutputList calc(FibonacciInputList list) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Request[{0}] - calc({1})", 
                    new Object[] {
                        list.getRequestId(), FibonacciInputList.class.getCanonicalName()
                    }));
        }
        ArrayList<FibonacciOutputItem> results = new ArrayList<>();
        List<FibonacciInputItem> inputs = list.getList();
        for(FibonacciInputItem input: inputs) {
            results.add(this.calc(input.getNumber()));
        }
        return new FibonacciOutputList(results);
    }
}
