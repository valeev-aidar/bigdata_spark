import com.github.catalystcode.fortis.spark.streaming.rss.RSSInputDStream
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.PipelineModel
import TextProcess.process

object Main {
  def main(args: Array[String]) {
    val durationSeconds = 30
    val conf = new SparkConf()
      .setAppName("Twitter Stream Processing")
      .setIfMissing("spark.master", "local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    val ssc = new StreamingContext(sc, Seconds(durationSeconds))
    //val model = PipelineModel.load("/user/edinburgh/model")

    val urlCSV = "http://queryfeed.net/tw?q=dog"
    val urls = urlCSV.split(",")
    val stream = new RSSInputDStream(urls, Map[String, String](
      "User-Agent" ->
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"
    ), ssc, StorageLevel.MEMORY_ONLY, pollingPeriodInSeconds = durationSeconds)

    stream.foreachRDD(rdd => {
      val spark = SparkSession
        .builder()
        .appName(sc.appName)
        .getOrCreate()
      import spark.sqlContext.implicits._

      println("---start-of-stream---")
      //rdd.toDS().foreach(e => println(e.description.value.toString.trim))
      val df = rdd.map(x => process(x.description.value)).toDF("text")
      //val predictions = model.transform(df).select("prediction").rdd.map(_.getDouble(0))
      //println(predictions)
      df.foreach(x => println(x.get(0)))
      println("---end-of-stream---")
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
