Test pub/sub

Scenario: Publish 10000 numbers

Given a Fibonacci PubsubHandler named 'test' with properties file: 'pubsub_subscriber.properties'
Then the PubsubHandler named 'test' connection is 'opened'
When I publish '10000' random messages to PubsubHandler named 'test'
And waiting for subscriber of PubsubHandler(test) finish
Then PubsubHandler named 'test' receives '10000' messages
When I close PubsubHandler named 'test'
Then the PubsubHandler named 'test' connection is 'closed'
