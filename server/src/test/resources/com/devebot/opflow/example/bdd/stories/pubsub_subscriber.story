Test pub/sub

Scenario: Publish 10000 numbers

Given a Fibonacci PubsubHandler(test) with properties file: 'pubsub_subscriber.properties'
Given '3' subscribers in PubsubHandler(test)
Then PubsubHandler(test) has exactly '3' consumers
Then the PubsubHandler(test)'s connection is 'opened'
When I publish '10000' random messages to PubsubHandler(test)
And waiting for subscriber of PubsubHandler(test) finish
Then PubsubHandler(test) receives '10000' messages
When I close PubsubHandler(test)
Then the PubsubHandler(test)'s connection is 'closed'
