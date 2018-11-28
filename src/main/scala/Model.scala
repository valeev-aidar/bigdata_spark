import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{Tokenizer, _}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.sql._
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.DoubleType

import TextProcess.process

object Model {
  def main(args: Array[String]) {

    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("Model")
      .getOrCreate()


    var df = spark.read.format("csv")
      .option("header", "true")
      //.load("file:///Users/gridm/Desktop/spark-recommendation/spark-recommendation/train.csv")
      .load("hdfs:///Sentiment/twitter/train.csv")
      .select("Sentiment", "SentimentText")
      .toDF("labelStr", "rawText")

    val clean = udf(process _)

    df = df.withColumn("text", clean(df("rawText")))
    df = df.withColumn("label", df("labelStr").cast(DoubleType))

    //val Array(train, test) = df.randomSplit(Array[Double](0.7, 0.3))
    val train = df

    val tokenizer = new Tokenizer()
      .setInputCol("text")
      .setOutputCol("words")
    val vectorizer = new CountVectorizer()
      .setInputCol("words")
      .setOutputCol("rawFeatures")
    val idf = new IDF()
      .setInputCol("rawFeatures")
      .setOutputCol("features")
    val lr = new LogisticRegression()
      .setMaxIter(1000)
      .setRegParam(1)
    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, vectorizer, idf, lr))

    val model = pipeline.fit(train)

    model.write.overwrite().save("/user/edinburgh/model")


    /*
    val paramGrid = new ParamGridBuilder()
      .addGrid(lr.regParam, Array(0.1, 1, 10))
      .build()

    val cv = new CrossValidator()
      .setEstimator(pipeline)
      .setEvaluator(new BinaryClassificationEvaluator)
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(3)

    val model = cv.fit(train)
    model.avgMetrics.foreach(println)
    */
    val sameModel = PipelineModel.load("/user/edinburgh/model")

    val res = sameModel.transform(train)

    val predictions = res.select("prediction").rdd.map(_.getDouble(0))
    val labels = res.select("label").rdd.map(_.getDouble(0))

    val accuracy = new MulticlassMetrics(predictions.zip(labels)).accuracy

    print(accuracy)

    //res.select("words", "probability", "prediction").show()


    spark.stop()

  }
}