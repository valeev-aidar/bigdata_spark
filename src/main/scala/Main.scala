import java.util.Date
import java.text.SimpleDateFormat

import com.github.catalystcode.fortis.spark.streaming.rss.RSSInputDStream
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.PipelineModel
import TextProcess.process

object Main {
  def main(args: Array[String]) {
    val durationSeconds = 90
    val conf = new SparkConf()
      .setAppName("Twitter Stream Processing")
      .setIfMissing("spark.master", "local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    val ssc = new StreamingContext(sc, Seconds(durationSeconds))

    val model = PipelineModel.read.load("/user/edinburgh/model")

    val query = if (args(0) != null && args(0).length > 0 && args(0)(0) != '-') args(0) else "hate+OR+love"
    val urlCSV = "http://queryfeed.net/tw?token=5bfec0d2-4657-4d2a-98d0-69f3584dc3b3&q=" + query
    val urls = urlCSV.split(",")
    val stream = new RSSInputDStream(urls, Map[String, String](
      "User-Agent" ->
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
    ), ssc, StorageLevel.MEMORY_ONLY, pollingPeriodInSeconds = durationSeconds,
      connectTimeout = 5000, readTimeout = 5000)

    stream.foreachRDD(rdd => {
      val spark = SparkSession
        .builder()
        .appName(sc.appName)
        .getOrCreate()
      import spark.sqlContext.implicits._

      if(rdd.count() != 0) {
        println("\n" + formatDate(new Date()))
        println("---start-of-stream---")

        val df = rdd.map(x => process(x.description.value)).toDF("text")

        model.transform(df)
          .select("text","prediction").rdd.collect()
          .map(formatPrediction)
          .foreach(println)

        println("---end-of-stream---\n\n")
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }

  def formatDate(d: Date): String =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d)

  def formatPrediction(x: Row): String =
    (if(x.getDouble(1) > 0) "[positive] "
    else "[negative] ") + x.getString(0)
}
