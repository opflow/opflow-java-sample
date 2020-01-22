# opflow-java-example


## Overview

![Arch](https://raw.github.com/opflow/opflow-java-sample/master/docs/assets/images/opflow-java-sample-arch.png)


## Prerequisites

### Install Docker

Install the Docker software by following the [installation instructions on the Docker website](https://www.docker.com/community-edition).


### Create the private network

Check the network for existent:

```shell
docker network ls | grep opflow-dev-net
```

Create the network `opflow-dev-net`:

```shell
docker network create opflow-dev-net
```

### Start the RabbitMQ server

```shell
docker run -d \
  --network=opflow-dev-net \
  --name opflow-rabbitmq-server \
  --hostname opflow-rabbitmq-server \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=giong \
  -e RABBITMQ_DEFAULT_PASS=qwerty \
  rabbitmq:3-management
```

### Setup the connection parameters

The connection for business flow:

* User: opuser, password: qwerty;
* Virtual host (vHost): opflow;
* Set full permissions for the user `opuser` to the vHost `opflow`;

The connection for logging flow:

* User: logger, password: qwerty;
* Virtual host (vHost): oplogs;
* Set full permissions for the user `logger` to the vHost `oplogs`;


## Quickstarts

### Start the master

Start the master module of the example from the docker image:

```shell
docker run -it --rm \
  -p 8888:8888 \
  -p 8989:8989 \
  --network=opflow-dev-net \
  --name=opflow-java-sample-master \
  opwire/opflow-java-sample-master:0.1.28 java \
    -Dlog4j.configuration=file:/app/config/log4j.properties \
    -Dlog4j.appender.opflowAMQP.enabled=true \
    -Dlog4j.appender.opflowAMQP.host=opflow-rabbitmq-server \
    -Dlog4j.appender.opflowAMQP.username=logger \
    -Dlog4j.appender.opflowAMQP.password=qwerty \
    -Dlog4j.appender.opflowAMQP.virtualHost=oplogs \
    -Dopflow.configuration=file:/app/config/master.properties \
    -Dopflow.commander.host=opflow-rabbitmq-server \
    -Dopflow.commander.username=opuser \
    -Dopflow.commander.password=qwerty \
    -Dopflow.commander.virtualHost=opflow \
    -Dopflow.commander.applicationId=FibonacciGenerator \
    -Dopflow.commander.rpcWatcher.enabled=false \
    -Dopflow.commander.rpcWatcher.interval=5000 \
    -jar /app/opflow-java-sample-master.jar
```

### Start the workers

For the workers:

```shell
docker run -it --rm \
  --network=opflow-dev-net \
  --name=opflow-java-sample-worker \
  opwire/opflow-java-sample-worker:0.1.28 java \
    -Dlog4j.configuration=file:/app/config/log4j.properties \
    -Dlog4j.appender.opflowAMQP.enabled=true \
    -Dlog4j.appender.opflowAMQP.host=opflow-rabbitmq-server \
    -Dlog4j.appender.opflowAMQP.username=logger \
    -Dlog4j.appender.opflowAMQP.password=qwerty \
    -Dlog4j.appender.opflowAMQP.virtualHost=oplogs \
    -Dopflow.configuration=file:/app/config/worker.properties \
    -Dopflow.serverlet.host=opflow-rabbitmq-server \
    -Dopflow.serverlet.username=opuser \
    -Dopflow.serverlet.password=qwerty \
    -Dopflow.serverlet.virtualHost=opflow \
    -Dopflow.serverlet.applicationId=FibonacciGenerator \
    -jar /app/opflow-java-sample-worker.jar
```

## Build from the source code


### Build `opflow-core` from the latest source code

Clone the `master` branch from github:

```shell
git clone https://github.com/opflow/opflow-java.git
cd opflow-java
```

Compile and install the package:

```shell
mvn clean install
```

### Clone the example source code

Clone source code from `github`:

```shell
git clone https://github.com/opflow/opflow-java-sample.git
cd opflow-java-sample
```

### Install the `fib-api` package

Build and install the `opflow-java-sample-fib-api.jar` and `opflow-java-sample-fib-api-tests.jar` into the local repository:

```
cd fib-api
mvn clean install
cd ../
```

### Install the `fib-impl` package

Build and install the `opflow-java-sample-fib-impl.jar` into the local repository:

```
cd fib-impl
mvn clean install
cd ../
```

### Run the workers

Open a new `terminal` and change to `opflow-java-sample/worker` directory.
Update the rabbitmq connection parameters in `src/main/resources/worker.properties`:

```properties
opflow.uri=amqp://giong:qwerty@opflow-rabbitmq-server
# ...
```

Compile `opflow-java-sample-worker` and start the worker (worker):

```shell
mvn clean compile exec:java -Pserver -Dfibonacci.calc.delay.min=5 -Dfibonacci.calc.delay.max=10
```

### Run the master

Open a new `terminal` and change to `opflow-java-sample/master` directory.
Update the rabbitmq connection parameters in `src/main/resources/master.properties`:

```properties
opflow.uri=amqp://giong:qwerty@opflow-rabbitmq-server
# ...
```

Compile `opflow-java-sample-master` and start the web service on master:

```shell
mvn clean compile exec:java -Pmaster
```

![Netbeans](https://raw.github.com/opflow/opflow-java-sample/master/docs/assets/images/opflow-netbeans-terminal.png)

### Try the `ping` and the `fibonacci` actions

Open the web browser and make a HTTP request to `http://localhost:8989/ping`:

![Ping](https://raw.github.com/opflow/opflow-java-sample/master/docs/assets/images/browser-get-ping.png)

Calculate the fibonacci of a number with url `http://localhost:8888/fibonacci/29`:

![Calc](https://raw.github.com/opflow/opflow-java-sample/master/docs/assets/images/browser-get-calc.png)


## Run TDD test

Compiles source code and installs dependencies:

```shell
mvn compile
```

Invokes maven to run unit tests:

```shell
mvn clean test
```


## Run BDD test

Compiles source code and installs dependencies:

```shell
mvn compile
```

Invokes maven to run integration tests:

```shell
mvn clean verify
```


## Packing

Packing and assembling program and dependencies into `jar` package:

```shell
mvn clean package -Pbundle
```

Copies and customizes `log4j.properties` and `opflow.properties` files:

```shell
cp src/main/resources/*.properties ~/
```

Opens and edit properties files if necessary to change.

Execute worker:

```shell
bash fibonacci --process worker
```

Make a request Fibonacci(38):


```shell
bash fibonacci -p master -r request -n 38
```

Calculate Fibonacci for 1000 random numbers in a range [20, 40]:

```shell
bash fibonacci -p master -r random --total 1000 --range 20,40
```
