# Introduction to Big Data 
# Assignment #2: Stream processing with Spark
Executable app.jar lies in our folder on the hadoop cluster.

## Team edinburgh
Vasiliy Alabugin, Dmitry Grigorev, Aidar Valeev, Nick Gaivoronskiy

## How to build the project 
* make sure you have sbt installed.
* make sure you are in the project directory
* ```set "JAVA_OPTS=-Xms256M -Xmx2g"``` (use ```export``` instead of ```set ``` for *nix)
* ```sbt assembly```

## How to run Spark job 
* make sure you are in the project directory
* make sure you have set the environment (use ```export``` instead of ```set ``` for *nix)
  * ```set HADOOP_CONF_DIR=path_to_hadoop_configuration```
  * ```set YARN_CONF_DIR=path_to_hadoop_configuration```
  * ```set HADOOP_USER_NAME=your_spark_user_name```
* Stream Processing: to submit locally
  * ```spark-submit --driver-memory 2g --executor-memory 2g path/to.jar -user false```
* Stream Processing: to submit to the cluster
  * ```spark-submit --master yarn path/to.jar -user false```
* To fit model: to submit to the cluster
  * ```spark-submit --master yarn --class Model path/to.jar -user false```
