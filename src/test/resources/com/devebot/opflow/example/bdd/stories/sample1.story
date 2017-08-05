Very simple story

Scenario: Execute single Fibonacci request

Given a Fibonacci master
And a Fibonacci worker: demo1
And a Fibonacci worker: demo2
When I make a request named fib30 with number: 30
Then the request fib30 should finished successfully
