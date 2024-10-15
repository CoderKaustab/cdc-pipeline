# Data Pipeline for CDC from MySQL to Various Sinks #

This project is designed to create a data pipeline that captures Change Data Capture (CDC) events from a source Mysql database and streams them to various sinks as depicted in the following diagram : 


![image](https://github.com/user-attachments/assets/9b56e1bb-e7ed-4172-bf33-2a74967c36a0)



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
