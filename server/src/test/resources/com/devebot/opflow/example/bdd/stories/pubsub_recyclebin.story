Test pub/sub

Scenario: Publish 10000 numbers

Given a Fibonacci PubsubHandler(test) with properties file: 'pubsub_recyclebin.properties'
Given '1' subscribers in PubsubHandler(test)
Then PubsubHandler(test) has exactly '1' consumers
Then the PubsubHandler(test)'s connection is 'opened'
Given a failed number arrays '60, 55, 70, 80, 90'
When I publish '10000' random messages to PubsubHandler(test)
And waiting for subscriber of PubsubHandler(test) finish
Then PubsubHandler(test) receives '10015' messages
When I close PubsubHandler(test)
Then the PubsubHandler(test)'s connection is 'closed'