## Needed stacks
    + maven 3
    + java8
    + nats

## Tech stacks
    + maven 3
    + java8
    + nats
    + springboot

## To get started follow this checklist:
    + install nats streaming server https://nats.io/documentation/streaming/nats-streaming-install/
    + run nats streaming server nats-streaming-server -m 8333 -p 4333
    + install nats server https://nats.io/documentation/tutorials/gnatsd-install/
    + run nats server  gnatsd -m 8222
    + mvn clean install -DskipTests
    + mvn test
    + for more at https://github.com/nats-io
