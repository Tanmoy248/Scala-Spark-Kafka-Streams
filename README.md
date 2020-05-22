**Software versions:**
Spark - 3.0.0-preview2
KafkaUtils - Streaming app between spark and kafka

Built this code with reference from : `https://spark.apache.org/docs/1.6.3/streaming-kafka-integration.html`

`Goals:`
Live data being pushed from user mobile to kafka.
Use streaming apps to report the people online and people available in last `n minutes`

Next Steps:
1. Persist the counts to DB, alongwith time, to create trends
2. Maintain a map so that late-arriving health pings are accounted for in the reports

Docker Images and setup:
1. sudo docker pull mongo
2. sudo docker run --name mongo-dev -d mongo:latest
