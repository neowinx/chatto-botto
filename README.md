# チャットボット (chatto-botto)

Probably the most over engineered chatbot you'll ever see

The implementation uses Akka Actors with Cluster that sends
messages between servers and clients

It's roughly based on the advanced example for akka 2.6.16
that uses the new typed especification

Also uses the recommended functional style to implement the actors

## Requirements

- [Java 8+](https://openjdk.java.net/install/)
- [SBT](https://www.scala-sbt.org/)

## Configuration

Configuration parameters are in the corresponding `application.conf` of 
the `client` and `server` projects

## Usage

It is recommended to stage the projects and use them from the console sicne sbt
has some erratic behavior when multiple instances are started 

- Stage and start the server project

```bash
sbt "project server ; stage"
./server/target/universal/stage/bin/server
```

- Stage and start the client project

```bash
sbt "project client ; stage"
./client/target/universal/stage/bin/client
```

- Enter `/help` for display the help in the client 

### Commands

```bash
- /login {NAME} - Start chato-botto session
- /quit - Quits chatto-botto
```

Once you have a session every text inserted in the client will be send to the corresponding actors 
through the Akka Cluster

# Enjoy

[neowinx](https://github.com/neowinx)
