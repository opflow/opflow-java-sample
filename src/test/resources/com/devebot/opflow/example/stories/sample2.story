A story is a collection of scenarios

Lifecycle: 
Before:
Given a Fibonacci master
And a Fibonacci worker: demo1
And a Fibonacci worker: demo2

Scenario: Get input data from Example table

When I make a request named [requestName] with number: [number]
Then the request [requestName] should finished successfully

Examples: 
|requestName| number |
| fibo21    | 21     |
| fibo26    | 26     |
