# bigdata_spark
Introduction to Big Data. Assignment 2. Stream Processing with Spark

### To compile jar
* make sure you have sbt installed.
* make sure you are in the project directory
* ```set "JAVA_OPTS=-Xms256M -Xmx2g"``` (```export``` instead of ```set ```for Unix)
* ```sbt assembly```

### To submit
* make sure you are in the spark directory
* make sure you have set the environment: (```export``` instead of ```set ```for Unix)
  * ```set HADOOP_CONF_DIR=path\to\hadoop"```
  * ```set YARN_CONF_DIR=path\to\hadoop"```
  * ```set HADOOP_USER_NAME=edinburgh"```
* **to submit locally**
*```spark-submit --driver-memory 2g --executor-memory 2g path/to.jar -user false```
* **to submit to the cluster**
  * ```spark-submit --master yarn path/to.jar -user false```

