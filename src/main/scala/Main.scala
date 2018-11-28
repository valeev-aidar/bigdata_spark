import com.github.catalystcode.fortis.spark.streaming.rss.RSSInputDStream
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.PipelineModel
import TextProcess.process

object Main {
  def main(args: Array[String]) {
    val durationSeconds = 60
    val conf = new SparkConf()
      .setAppName("Twitter Stream Processing")
      .setIfMissing("spark.master", "local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    val ssc = new StreamingContext(sc, Seconds(durationSeconds))

    val model = PipelineModel.load("/user/edinburgh/model")

    val urlCSV = "https://queryfeed.net/tw?token=5bfec0d2-4657-4d2a-98d0-69f3584dc3b3&q=weather"
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

      var df = rdd.map(x => process(x.description.value)).toDF("text")

      if(rdd.count() == 0)
        df = spark.createDataFrame(getDefaultData).toDF("text", "label")

      model.transform(df)
        .select("text","prediction").rdd.collect()
        .map(formatPrediction)
        .foreach(println)

      println("---end-of-stream---")
    })

    ssc.start()
    ssc.awaitTermination()
  }

  def formatPrediction(x: Row): String =
    (if(x.getDouble(1) > 0) "[positive] "
    else "[negative] ") + x.getString(0)

  def getDefaultData = Seq(
    ("last season monte livata love this day snowalp the midzone nice weather frosty snow and nobody the spot blackdiamond thenorthface salomon msr climbingtechnology ferrino timetoplay", 0.0),
    ("whats the weather going over next few days mid sussex can tell you but only you tune online see you there weather weatherfortheweekahead the meantime heres clue", 0.0),
    ("rgv weather txwx mcallen weslaco harlingen brownsville right now cloudy skies some very light rain starr county", 0.0),
    ("walking the rain album icfair rotterdam weather rain citylife visual rainy", 0.0),
    ("right now haze temperature humidity wind from wsw updated delhi weather", 0.0),
    ("snow picnic setting black creek today weather onstorm", 0.0),
    ("the met office has issued yellow weather warning for wind rain tomorrow keep your speed down driving cycling remember have lights reflective clothing", 0.0),
    ("gwlyb wet gwyntog windy tywydd weather mwy", 0.0),
    ("more economic transport disruptions devon due increasing erratic weather relocating the issue not enough action urgent need reduce emissions especially ukgov banks enablers largest culprits extinctionrebellion", 0.0),
    ("meteo castrovillari calabria ore temperatura umidit bar hpa pioggia vento weather weathercloud arpacal meteo eteonepb psonmeteo puglia", 0.0)
  )
}
