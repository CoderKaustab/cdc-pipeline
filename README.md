# CDC Data Pipeline from MySQL to Various Sinks #

This project is designed to create a data pipeline that captures Change Data Capture (CDC) events from a source Mysql database and streams them to various sinks as depicted in the following diagram : 


![image](https://github.com/user-attachments/assets/9b56e1bb-e7ed-4172-bf33-2a74967c36a0)



<br>
<br>

# Uses #

1. Lightweight and thus a good alternative to database trigger
2. OLTP to OLAP conversion - useful for database audit
3. Creating data replication
4. Sharing data between microservices in real-time
5. Data can be synced in various data centers like - HDFS, Redis, etc maintaining eventual consistency which is a good alternative of traditional ETL process.

<br>
<br>

# Disadvantage: #

1. Needs to connect with Master database with bin log enabled. It doesn't work on read replicas. 
2. Connecting to different sources & sinks may cause PCI problems & security challenges.
3. While creating consistent snapshots it acquires read/write locks so there can be a downtime while Debezium creates a snapshot. We need to configure the connector properly so that snapshot time can be kept minimum. Or else we need to opt for some other snapshot strategy. For details you can go through the Debexium official website here - https://debezium.io/documentation/

<br>
<br>

### Start the application: ###

1. Download the application to your local machine.

2. Switch to the master branch if the current branch is not pointing to the master branch.

3. Open the src/main/resources/application.properties file.

4. First, check the property pipeline.registry.file. By default, this should point to cdc/pipeline_registry.json, which is the file read by the application to register all the pipeline configurations. You can change this path if you want to use a different registry file.

5. Next, check the property jasypt. password, which is used to decrypt any Jasypt-encrypted passwords. You will need to provide your Jasypt secret here.

6. Now, open the file located at src/main/resources/cdc/pipeline_registry.json as specified by the pipeline.registry.file property in application.properties.

7. In this file, add the path of your pipeline configuration file in the pipeline_metadata_files array. For the demo, we have included the cdc/mysql-cdc-cqrs-pipeline.json file, which creates a CDC pipeline between a MySQL source database and various sink databases.

8. Open the cdc/mysql-cdc-cqrs-pipeline.json file. Update the database URLs, usernames, and passwords for the source database. Please note that the password should be provided in Jasypt-encrypted format. You can review the Debezium connector properties here and adjust them as per your requirements.

9. Next, check the sink section in the cdc/mysql-cdc-cqrs-pipeline.json file. We have added configurations for all the supported sinks (e.g., Kafka, Redis, JDBC databases). You can add or remove sinks based on your needs. Update the necessary configurations such as the Kafka server URL, Redis database URL, usernames, and passwords (in Jasypt-encrypted format). You can also manage the sink operations from this section according to your requirements.

10. Launch your application.

11. Upon launching, you will notice that all the sinks—such as JDBC templates for databases, Redis clients, Kafka consumers, etc.—are created and added to the Spring context automatically, as defined in the pipeline metadata files.

12. Once the sinks are initialized, the MySQL Debezium connector is started, which will first capture a database snapshot and then begin reading the binlog for CDC. Please note that the Debezium connector will fail to launch if the MySQL source database does not have binary logging (binlog) enabled, as this is a pre-requisite for Debezium. You can check whether binlog is enabled by running the following MySQL command:

    SHOW VARIABLES LIKE 'log_bin';




<br>
<br>


### Source Types: ###

Currently, the project supports only the Mysql Debezium Connector. 

1. If any customization is required, that can be done by updating the code of - co.kaustab.cdc.listener.MysqlDefaultCdcListener.

2. In case a new connector type (e.g: Mongo Debezium connector) is required, that can be done in the following way:
    a. Add the deuterium-connector dependency in the pom.xml
    b. Create a child class of BaseCdcListener in the co.kaustab.cdc.listener package.
    c. Override the handleCdcEvent() method.
    d. In the pipeline file point source.class value to your newly created CDCListener class.
    e. Update the source.source_config as per your requirement.


<br>
<br>

### Sink Types: ###

Currently, only the following sink types are supported: 

1. Mysql db
2. Mongodb
3. Kafka
4. Redis

If any customization is required for the existing sink, then we need to change the implementation of the Spring bean class defined in sinks.bean_service.
If a new sink type is needed then the following steps can be performed: 
  1. Add the dependencies in the pom.xml file
  2. Add service class in the co.kaustab.cdc.service.sink package implementing the SinkService interface.
  3. Provide the implementation for the sink() method. 
  4. You will be able to add a sink in the pipeline metadata file (mysql-cdc-cqrs-pipeline.json file in our example) with a suitable bean name and the properties.
  5. Now, you will need to launch the sink (e.g. Kafka consumer, AWS client, etc) on the application startup. 
     For this, you will need to add an implementation of the SinkConfigRegistry interface under the co.kaustab.cdc.config package. 
  6. Once done add this newly created SinkConfigRegistry in the initSink() method of the DynamicPipelineRegistrationConfiguration class and launch your application.

<br>
<br>

### Monitoring: ###

1. Using the following command we can check the bin log read by the Debezium connector: 

    => bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic offset-storage-topic --from-beginning 
        {"file":"mysql-bin-changelog.000381","pos":120410681,"row":1,"snapshot":true}
        {"file":"mysql-bin-changelog.000382","pos":396495,"row":1,"snapshot":true}

   The Kafka server and the topic details can be configured in the pipeline file -> source.source_config.config.bootstrap.servers, source.source_config.config.offset.storage.topic

   You can also check the latest tails from - http://localhost:8084/mysql-rds-connector-v12/tail on the browser. 

2. You can check the "received=", "struct ---->", "Data Changed with Operation" in the log of the application. 
