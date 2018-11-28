import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector}
import org.apache.spark.ml.feature._
import org.apache.spark.sql._
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object Model {
  case class Twitt(label: Int, sentence: String)


  def main(args: Array[String]) {

    val col1 = "_1"
    val col2 = "_2"

    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("example")
      .getOrCreate()



    //val df = spark.read.format("csv").option("header", "true").load("csvfile.csv")
    var df = spark.createDataFrame(Seq(
      (0.0, "Hi I heard about Spark"),
      (1.0, "I wish Java could use case classes"),
      (1.0, "Logistic regression models are neat")
    )).toDF("label", "text")


    var test = spark.createDataFrame(Seq(
      (0.0, "I heard about Spark"),
      (1.0, "I wish Java")
    )).toDF("label", "text")

    val tokenizer = new Tokenizer() // standard method in Spark ML
      .setInputCol("text")
      .setOutputCol("words")
    val vectorizer = new CountVectorizer()
      .setInputCol("words")
      .setOutputCol("rawFeatures")
    val idf = new IDF()
      .setInputCol("rawFeatures")
      .setOutputCol("features")
    val lr = new LogisticRegression()
      .setMaxIter(10)
      .setRegParam(0.001)
    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, vectorizer, idf, lr))

    val model = pipeline.fit(df)
    // Make predictions on test documents.
    val res = model.transform(test)

    res.select("words", "probability", "prediction").show()
    //println(res)


    /*
        import spark.implicits._

        val typedRDD = clean(df).map{case Row(label: Double, sentence: String) => (label, sentence)}
        val cleanedDF = spark.createDataFrame(typedRDD)

        val tokenizer = new Tokenizer().setInputCol(col2).setOutputCol("words")
        val wordsData = tokenizer.transform(cleanedDF)

        val vectorizer = new CountVectorizer()
          .setInputCol("words").setOutputCol("rawFeatures")
        val model = vectorizer.fit(wordsData)
        val featurizedData = model.transform(wordsData)


        val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
        val idfModel = idf.fit(featurizedData)

        val rescaledData = idfModel.transform(featurizedData)
        val trainFeatures = features(rescaledData)

        //var input = asScalaBuffer(trainFeatures.map(row => row.getAs[SparseVector](0))).toDF("features")

        var input = trainFeatures.map(row => row.getAs[SparseVector](0)).toDF("label")


        input = mergeDF(input, df)


        val convertedVecDF = MLUtils.convertVectorColumnsToML(input)

        val lr = new LogisticRegression()
          .setMaxIter(100)
          .setRegParam(0.3)

        // Fit the model
        val lrModel = lr.fit(convertedVecDF)

        //Testing
        var test = spark.createDataFrame(Seq(
          (0.0, "WTF I heard???"),
          (1.0, "I wish Java")
        )).toDF(col1, col2)

        val newRDD = clean(test).map{case Row(label: Double, sentence: String) => (label, sentence)}
        val newDF = spark.createDataFrame(newRDD)

        val wordsTest = tokenizer.transform(newDF)
        val testData = model.transform(wordsTest)

        val newData = idfModel.transform(testData)
        val testFeatures = features(newData)

        var testInput = asScalaBuffer(testFeatures.map(row => row.getAs[Array[Double]](0))).toDF("features")

        testInput = mergeDF(testInput, test)


        println(lrModel.coefficientMatrix)
    */
    //rdd.map(_.toString()).saveAsTextFile("/user/team3/vec")
    //rescaledData.select("label", "features").show()
    spark.stop()

  }

  def clean(df: DataFrame): RDD[Row] = {
    df.rdd.map(row => {
      val text = row.getAs[String](1)
      val make = text.replaceAll("[^A-Za-z0-9 ]", "")
      Row(row(0),make)
    })
  }

  def features(rescaledData: org.apache.spark.sql.Dataset[Row]): ListBuffer[Row] = {
    val list = rescaledData.select("rawFeatures","features").collectAsList().toList

    val features = ListBuffer[Row]()

    for (row <- list) {
      val tf = row.getAs[SparseVector](0).toArray
      val idf = row.getAs[SparseVector](1).toArray

      val arr = new Array[Double](tf.length)

      for (i <- tf.indices )
        arr(i) = tf(i) * idf(i)

      val vec = new SparseVector(idf.length, idf.indices.toArray, arr)

      features.append(Row(vec))
    }

    features
  }

  def mergeDF(df1: DataFrame, df2: DataFrame): DataFrame = {
    val in1 = df1.withColumn("rowId1", monotonically_increasing_id())
    val in2 = df2.withColumn("rowId2", monotonically_increasing_id())

    val output = in1.as("df1").join(in2.as("df2"), in1("rowId1") === in2("rowId2"), "inner").
      select("df2._1", "df1.features")

    output.toDF("label", "features")
  }

}
