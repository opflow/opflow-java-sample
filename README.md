

## Prerequisites

### Install Rabbitmq on Ubuntu

Add the APT repository to your `/etc/apt/sources.list.d`:

```shell
echo 'deb http://www.rabbitmq.com/debian/ testing main' |
     sudo tee /etc/apt/sources.list.d/rabbitmq.list
```

Add Rabbitmq public key to our trusted key list using `apt-key`:

```shell
wget -O- https://www.rabbitmq.com/rabbitmq-release-signing-key.asc |
     sudo apt-key add -
```

Update the package list:

```shell
sudo apt-get update
```

Install rabbitmq-server package:

```shell
sudo apt-get install rabbitmq-server
```

## Clone and Run example

Clone source code from `github`:

```shell
git clone https://github.com/devebot/opflow-java-example.git
```

Change to project folder and install dependencies using `maven`:

```shell
$ mvn compile
```

To run worker, use the following command:

```
$ mvn compile exec:exec -Dworker
```

And master:

```
$ mvn compile exec:exec -Dmaster
```
