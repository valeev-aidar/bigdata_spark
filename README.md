# Introduction to Big Data assignment #2: stream processing with Spark 
## How to build the project 
* make sure you have sbt installed.
* make sure you are in the project directory
* run ```set "JAVA_OPTS=-Xms256M -Xmx2g"``` (use ```export``` instead of ```set ``` for *nix)
* run ```sbt assembly```

## How to run Spark job 
* make sure you are in the project directory
* make sure you have set the environment (use ```export``` instead of ```set ``` for *nix)
  * ```set HADOOP_CONF_DIR=path_to_hadoop_configuration```
  * ```set YARN_CONF_DIR=path_to_hadoop_configuration```
  * ```set HADOOP_USER_NAME=your_spark_user_name```
* to bumit locally, run
  * ```spark-submit --driver-memory 2g --executor-memory 2g path/to.jar -user false```
* to submit to the cluster, run
  * ```spark-submit --master yarn path/to.jar -user false```

