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

For idempotent pipelines, refer : https://spark.apache.org/docs/latest/streaming-kafka-0-10-integration.html#kafka-itself

    In the direct stream approach, spark-kafka stream will create 1 RDD
    for each partition of the topic.
    Hence, we can simply save the max(offset) for a given topic and 
    partition in mongo collection.
    This offset will be used in the next streaming context startup, 
    if the app crashes
   
*Common Errors and Fixes:*
1. Kafka network issues; check that the `kafka-python` entry is valid in /etc/hosts of executor-machine and kafka-docker
Kafka docker machine should have /etc/hosts like this :
```
    >127.0.0.1	localhost
    ::1	localhost ip6-localhost ip6-loopback
    fe00::0	ip6-localnet
    ff00::0	ip6-mcastprefix
    ff02::1	ip6-allnodes
    ff02::2	ip6-allrouters
    172.18.0.3    kafka-python
```

To rerun after mongodb cleanup:
 > db.kafka_meta_table.remove({})
   WriteResult({ "nRemoved" : 1 })
   > db.report_summary.remove({})
   WriteResult({ "nRemoved" : 6 })    

Now change the consumer-group in application.conf and rerun