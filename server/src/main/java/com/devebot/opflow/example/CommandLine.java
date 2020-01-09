package com.devebot.opflow.example;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.devebot.opflow.OpflowRpcResult;
import com.devebot.opflow.OpflowTimeout;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.exception.OpflowBootstrapException;
import com.devebot.opflow.exception.OpflowConnectionException;
import com.devebot.opflow.exception.OpflowOperationException;
import com.google.common.base.Stopwatch;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author drupalex
 */
public class CommandLine {
    
    public static void main(String ... argv) {
        CommandLine main = new CommandLine();
        JCommander.newBuilder().addObject(main).build().parse(argv);
        main.dispatch();
    }
    
    @Parameter(names={"--process", "-p"}, description = "Program mode (client/server)")
    String mode;
    
    @Parameter(names={"--routine", "-r"}, description = "Client action: request/random/publish")
    String action;
    
    @Parameter(names={"--number", "-n"})
    int number = 0;
    
    @Parameter(names={"--total", "-t"})
    int total = 0;
    
    @Parameter(names={"--range", "-b"})
    String range = null;
    
    @Parameter(names={"--json", "-j"})
    String json;

    @Parameter(names={"--number-max"})
    int numberMax = 40;
    
    @Parameter(names={"--progress-enabled"})
    boolean progressEnabled = true;
    
    public void dispatch() {
        FibonacciRpcMaster master = null;
        FibonacciRpcWorker worker = null;
        FibonacciPublisher sender = null;
        FibonacciSubscriber pubsub = null;
        try {
            if ("server".equals(mode)) {
                final FibonacciSetting setting = new FibonacciSetting();

                System.out.println("[+] start Fibonacci RPC Worker ...");
                worker = new FibonacciRpcWorker(setting);
                worker.process();

                System.out.println("[-] start Fibonacci Setting Subscriber ...");
                pubsub = new FibonacciSubscriber(setting);
                pubsub.subscribe();

                System.out.println("[*] Waiting for message. To exit press CTRL+C");
            } else {
                Stopwatch stopwatch = Stopwatch.createStarted();
                if("request".equals(action)) {
                    System.out.println("[+] request Fibonacci(" + number + ")");
                    master = new FibonacciRpcMaster();
                    printResult(0, number, master.request(number).extractResult());
                    master.close();
                } else if ("random".equals(action)) {
                    if (total <= 0) total = 10;
                    int[] rangeInt = parseRange(range);
                    if (rangeInt.length != 2) {
                        rangeInt = new int[] {20, 40};
                    } else {
                        if (rangeInt[0] < 10) rangeInt[0] = 10;
                        if (50 < rangeInt[1] || rangeInt[1] <= rangeInt[0]) rangeInt[1] = 50;
                    }
                    
                    int countCompleted = 0;
                    System.out.println("[+] calculate Fibonacci() of " + total + " random number in range [" +
                            rangeInt[0] + ", " + rangeInt[1] + "].");
                    OpflowTimeout.Countdown countdown = new OpflowTimeout.Countdown(total);
                    master = new FibonacciRpcMaster();
                    Queue<FibonacciData.Pair> queue = master.random(total, rangeInt[0], rangeInt[1]);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {}
                    while(!queue.isEmpty()) {
                        FibonacciData.Pair req = queue.poll();
                        OpflowRpcResult result = req.getSession().extractResult();
                        printResult(req.getIndex(), req.getNumber(), result);
                        if (result.isCompleted()) countCompleted += 1;
                        countdown.check();
                    }
                    countdown.bingo();
                    System.out.println("[-] random command has been finished: " + countCompleted);
                    master.close();
                } else {
                    sender = new FibonacciPublisher();
                    sender.publish(numberMax);
                    sender.close();
                    System.out.println("[-] numberMax has been sent");
                }
                stopwatch.stop();
                System.out.println("[*] Time elapsed for " + action + " is " + 
                        stopwatch.elapsed(TimeUnit.MILLISECONDS) + " (ms)");
            }
        } catch (Exception exception) {
            System.out.println("[+] Error:");
            if (exception instanceof OpflowConnectionException) {
                printErrors(new String[] {
                    "[-] Connect to RabbitMQ has been failed. Try again with log4j TRACE mode.",
                    "[-] Verify the Connection Parameter/{URI, host,...} on console or logfile.",
                });
            } else if (exception instanceof OpflowBootstrapException) {
                printErrors(new String[] {
                    "[-] Initialize broker has been failed.",
                });
            } else if (exception instanceof OpflowOperationException) {
                printErrors(new String[] {
                    "[-] Invoke produce()/consume() has been failed.",
                });
            }
            printErrors(new String[] {
                "[+] Exception details:",
                "[-] class: " + exception.getClass().getName(),
                "[-] message: " + exception.getMessage()
            });
            // close all of connections
            if ("server".equals(mode)) {
                if (worker != null) worker.close();
                if (pubsub != null) pubsub.close();
            } else {
                if (master != null) master.close();
                if (sender != null) sender.close();
            }
        }
    }
    
    private void printResult(int index, int number, OpflowRpcResult result) {
        for(OpflowRpcResult.Step step: result.getProgress()) {
            System.out.println("[-] #" + index + " Fibonacci(" + number + ") percent: " + step.getPercent());
        }
        if (result.isTimeout()) {
            System.out.println("[-] #" + index + " Fibonacci(" + number + ") is timeout.");
        }
        if (result.isFailed()) {
            System.out.println("[-] #" + index + " Fibonacci(" + number + ") is failed: " + result.getErrorAsString());
        }
        if (result.isCompleted()) {
            System.out.println("[-] #" + index + " Fibonacci(" + number + ") -> " + result.getValueAsString());
        }
        System.out.println();
    }
    
    private void printErrors(String[] msgs) {
        for(String msg: msgs) {
            System.out.println(msg);
        }
    }
    
    private int[] parseRange(String rangeStr) {
        String[] rangeArr = OpflowUtil.splitByComma(rangeStr);
        int[] rangeInt = new int[rangeArr.length];
        for(int i=0; i<rangeInt.length; i++) {
            try {
                rangeInt[i] = Integer.parseInt(rangeArr[i]);
            } catch (NumberFormatException nfe) {}
        }
        return rangeInt;
    }
}
