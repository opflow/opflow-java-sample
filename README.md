# opflow-java-example

## Prerequisites

### Install Rabbitmq on Ubuntu

Add the APT repository to your `/etc/apt/sources.list.d`:

```shell
echo 'deb http://www.rabbitmq.com/debian/ testing main' | sudo tee /etc/apt/sources.list.d/rabbitmq.list
```

Add Rabbitmq public key to our trusted key list using `apt-key`:

```shell
wget -O- https://www.rabbitmq.com/rabbitmq-release-signing-key.asc | sudo apt-key add -
```

Update the package list:

```shell
sudo apt-get update
```

Install rabbitmq-server package:

```shell
sudo apt-get install rabbitmq-server
```

### Build from the latest source code

Clone the `master` branch from github:

```shell
git clone https://github.com/opflow/opflow-java.git
cd opflow-java
```

Compile and install the package:

```shell
mvn clean install
```

## Run example

Clone source code from `github`:

```shell
git clone https://github.com/opflow/opflow-java-sample.git
cd opflow-java-sample
```

### Install API/common

Install `opflow-java-sample-common.jar` and `opflow-java-sample-common-tests.jar`:

```
cd common
mvn clean install
cd ../
```

### Run the workers

Open a new `terminal` and change to `opflow-java-sample` directory.

Compile `opflow-java-sample-server` and start the server (worker):

```shell
cd server
mvn clean compile exec:java -Pserver
```

### Run the client

Open a new `terminal` and change to `opflow-java-sample` directory.

Compile `opflow-java-sample-client` and start the web service on client:

```shell
cd client
mvn clean compile exec:java -Pclient
```

## Old version examples

Clone source code from `github`:

```shell
git clone https://github.com/opflow/opflow-java-sample.git
cd opflow-java-sample
```

### Install API/common

Install `opflow-java-sample-common.jar` and `opflow-java-sample-common-tests.jar`:

```
cd common
mvn clean install
cd ../
```

### Install server

Compile `opflow-java-sample-server`:

```shell
cd server
mvn compile
```

To run worker, use the following command:

```shell
mvn compile exec:java -Pworker
```

And master:

```shell
mvn compile exec:java -Pmaster
```

And Pub/Sub:

```shell
mvn compile exec:java -Ppubsub
```

## Run TDD test

Compiles source code and installs dependencies:

```shell
mvn compile
```

Invokes maven to run unit tests:

```shell
mvn test
```

## Run BDD test

Compiles source code and installs dependencies:

```shell
mvn compile
```

Invokes maven to run integration tests:

```shell
mvn verify
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

Execute server:

```shell
bash fibonacci --process server
```

Make a request Fibonacci(38):


```shell
bash fibonacci -p client -r request -n 38
```

Calculate Fibonacci for 1000 random numbers in a range [20, 40]:

```shell
bash fibonacci -p client -r random --total 1000 --range 20,40
```
