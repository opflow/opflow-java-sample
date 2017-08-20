Very simple story

Scenario: Execute single Fibonacci request

Given a Fibonacci master
And a Fibonacci RpcWorker(demo1)
And a Fibonacci RpcWorker(demo2)
When I make a request named fib30 with number: 30
Then the request fib30 should finished successfully
When I close RpcWorker(demo1)
Then the RpcWorker(demo1)'s connection status is 'closed'
When I close RpcWorker(demo2)
Then the RpcWorker(demo2)'s connection status is 'closed'
