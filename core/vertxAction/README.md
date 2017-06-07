# Vert.x Actions for OpenWhisk

This project provide a way to invokes action developed with Eclipse Vert.x (http://vertx.io). It provides an 
asynchronous non-blocking way to develop actions, close to node.js.

## Example of actions

In java, develop an action as follows:

```java
package me.escoffier.openwhisk;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class MyVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().<JsonObject>consumer("invocation", message -> {
            JsonObject input = message.body();
            String name = input.getString("name", "stranger");
            message.reply(new JsonObject()
                .put("echo", message.body())
                .put("greeting", "welcome to Vert.x, " + name));
        });
    }
}
```

With RX Java:

```java
package me.escoffier.openwhisk;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;

public class MyRXVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().<JsonObject>consumer("invocation").toObservable()
            .subscribe(
                message -> {
                    String name = message.body().getString("name", "stranger");
                    message.reply(new JsonObject()
                        .put("echo", message.body())
                        .put("greeting", "welcome to Vert.x, " + name));
                });
    }
}
```

With Kotlin:

```kotlin
package me.escoffier.openwhisk

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject

class MyKotlinFunction : AbstractVerticle() {

    override fun start() {
        vertx.eventBus().consumer<JsonObject>("invocation").handler {  message ->
            var json = message.body()
            var name = json.getString("name", "stanger")
            message.reply(JsonObject().put("greeting", "hello " + name + ", welcome to Vert.x and Kotlin"))
        }
    }
}
```

## Create action

1. Package your action (verticle) in a regular jar.
2. Create your action using:
```bash
wsk --insecure action create <NAME> target/simple-java-action-1.0-SNAPSHOT.jar --main <VERTICLE> --kind vertx:default
```
3. Invoke your action as follows:
```bash
wsk --insecure action invoke --result <NAME> --param name Clement
```

## TODO List

* Load function from sources (non-compiled)
* Discuss a better way to develop and invoke action
* Show how you can invoke other services
* Provide logs
* Non blocking action by default (there are necessarily non blocking, but marked as blocking in openwhisk)
* Action not consuming / producing json
