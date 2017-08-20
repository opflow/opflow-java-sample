A story is a collection of scenarios

Lifecycle: 
Before:
Given a Fibonacci master
And a Fibonacci RpcWorker(demo1)
And a Fibonacci RpcWorker(demo2)
After:
When I close RpcWorker(demo1)
Then the RpcWorker(demo1)'s connection status is 'closed'
When I close RpcWorker(demo2)
Then the RpcWorker(demo2)'s connection status is 'closed'

Scenario: Get input data from Example table

When I make a request named [requestName] with number: [number]
Then the request [requestName] should finished successfully

Examples: 
|requestName| number |
| fibo21    | 21     |
| fibo26    | 26     |
