Simple Serverlet story

Lifecycle:
Before:
Given a Commander named 'commander1' with properties file: 'client.properties'
After:
Outcome: ANY
When I close Commander named 'commander1'

Scenario: Simple Serverlet
Given a registered FibonacciCalculator interface in Commander named 'commander1'
When I send a request to Commander 'commander1' to calculate fibonacci of '34'
