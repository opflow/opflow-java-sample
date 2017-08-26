package com.devebot.opflow.example;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.devebot.opflow.OpflowRpcRequest;
import com.devebot.opflow.OpflowRpcResult;
import com.devebot.opflow.OpflowUtil;
import com.devebot.opflow.exception.OpflowConnectionException;

/**
 *
 * @author drupalex
 */
public class CommandLine {
    
    public static void main(String ... argv) throws Exception {
        CommandLine main = new CommandLine();
        JCommander.newBuilder().addObject(main).build().parse(argv);
        main.dispatch();
    }
    
    @Parameter(names={"--program", "-p"}, description = "Program mode (client/server)")
    String mode;
    
    @Parameter(names={"--action", "-a"}, description = "Client action: request/publish")
    String action;
    
    @Parameter(names={"--number", "-n"})
    int number = 0;
    
    @Parameter(names={"--number-max", "-m"})
    int numberMax = 40;
    
    @Parameter(names={"--json", "-j"})
    String json;

    public void dispatch() throws Exception {
        try {
            if ("server".equals(mode)) {
                final FibonacciSetting setting = new FibonacciSetting();

                System.out.println("[+] start Fibonacci RPC Worker ...");
                final FibonacciRpcWorker worker = new FibonacciRpcWorker(setting);
                worker.process();

                System.out.println("[-] start Fibonacci Setting Subscriber ...");
                final FibonacciPubsubHandler pubsub = new FibonacciPubsubHandler(setting);
                pubsub.subscribe();

                System.out.println("[*] Waiting for message. To exit press CTRL+C");
            } else {
                if("request".equals(action)) {
                    System.out.println("[+] request Fibonacci(" + number + ")");
                    FibonacciRpcMaster master = new FibonacciRpcMaster();
                    OpflowRpcRequest session = master.request(number);
                    printResult(OpflowUtil.exhaustRequest(session));
                    master.close();
                } else {
                    FibonacciPubsubHandler sender = new FibonacciPubsubHandler();
                    sender.publish(numberMax);
                    sender.close();
                    System.out.println("[-] numberMax has been sent");
                }
            }
        } catch (OpflowConnectionException exception) {
            String[] msgs = new String[] {
                "[-] Connect to RabbitMQ has been failed. Try again with log4j TRACE mode.",
                "[-] Verify the Connection Parameter/{URI, host,...} on console or logfile.",
                "[-] Exception message: " + exception.getMessage()
            };
            for(String msg: msgs) {
                System.out.println(msg);
            }
        }
    }
    
    private void printResult(OpflowRpcResult result) {
        System.out.println("[-] ConsumerID: " + result.getWorkerTag());
        for(OpflowRpcResult.Step step: result.getProgress()) {
            System.out.println("[-] Fibonacci(" + number + ") percent: " + step.getPercent());
        }
        if (result.isTimeout()) {
            System.out.println("[-] Fibonacci(" + number + ") is timeout.");
        }
        if (result.isFailed()) {
            System.out.println("[-] Fibonacci(" + number + ") is failed: " + result.getErrorAsString());
        }
        if (result.isCompleted()) {
            System.out.println("[-] Fibonacci(" + number + ") -> " + result.getValueAsString());
        }
        System.out.println();
    }
}
