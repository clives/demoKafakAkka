# Demo project - Actor/Kafka 

## Current state

### done 
producer

consumer-slick(streams/slick)
 
consumer-actors 

### missing 

spec for the actors without kafka 

spec for the db - and a better dao model

integration test (consumer + producer )

using sbt, the test never stop (works fine in intelliJ), to confirm

.....

##  Design  choice

### sbt project
Multi sub projects but using only one project in the IDE(in case of future issue for the size:7mind/sbtgen)

base: define  basic  requirements for akka/kafka/testing - include simple kafka test for the json reading /writing 

consumer: stream - consume the events, then keep them in postgres using slick/kafka library

producer: simple producer - 

integration-test: wip -test of the consumer + producer

### Scala version

Scala 2.12.x as we have a short timeline, 3.00 if we had more flexilibity on the delivery

### Akka

- typed: no, as the typed actor are still marked as "may change".
- producer: one main actor as kafka consumer, receiving events from the "devices"
- consumer: one main actor receiving events from the kafka consumer, updating the devices(one actor per device)

### Kafka

we use alpakka from lightbend. The other alternative from cakesolutions is not maintenad anymore.

###  DB

for simplicity we choosed postgres, it should be enough  for the writing, but for the reading,
as  we use raw/json data, the performance are going to be a problem.
We keep the data in  String instead of json to simplify the application (time limit)

### Open question - that we could not ask for  limited contact/time  

- can we have more than one measure type per device? 
- can we have same measure using different unit ?(F° / C°)
- how many request per second should we expect with "real"  device?
- should we add a load  test module, like using gatling ? 
- the 4h was a real limitation ?  ( it  seem difficult to produce anything interesting in 4h  without copy/paste..)

### Testing

use TestContainers for kafka and in memory db.