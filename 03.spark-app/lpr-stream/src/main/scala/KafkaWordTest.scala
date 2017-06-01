import java.util.HashMap

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.elasticsearch.spark._

/**
  * Consumes messages from one or more topics in Kafka and does wordcount.
  * Usage: KafkaWordCount <zkQuorum> <group> <topics> <numThreads>
  *   <zkQuorum> is a list of one or more zookeeper servers that make quorum
  *   <group> is the name of kafka consumer group
  *   <topics> is a list of one or more kafka topics to consume from
  *   <numThreads> is the number of threads the kafka consumer should use
  *
  * Example:
  *    `$ bin/run-example \
  *      org.apache.spark.examples.streaming.KafkaWordCount zoo01,zoo02,zoo03 \
  *      my-consumer-group topic1,topic2 1`
  */
object KafkaWordTest {
  def main(args: Array[String]) {

    val zkQuorum = "172.16.118.133:2181"
    val group = "lpr-group"
    val topics = "LprData2"
    val numThreads = "1"

    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("KafkaWordTest")
//    sparkConf.set("es.index.auto.create","true")
//    sparkConf.set("spark.driver.allowMultipleContexts", "true")
//    sparkConf.set("es.index.auto.create", "true")
//    sparkConf.set("es.nodes.discovery", "true")
//    sparkConf.set("es.nodes", "192.168.56.101")
//    sparkConf.set("es.port", "9200")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("checkpoint")

    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
    val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)

    //val words = lines.flatMap(_.split(" "))

    val arrKey = Array("LocalNo","Speed","CameraId","Lane","Date","Time","Carnum","PlateX","PlateY",
      "PlateW","PlateH","PlateTag","VioCode","VioType","SensUniqCode","Direction","LmtSpeed","VioSpeed",
      "CarHeight","CarWidth","DateTM")
    val convertedwords = lines.map(_.toString())
    val wordCounts = convertedwords.map(x => Map( arrKey(0) -> x.substring(7,12)
      ,arrKey(1) -> x.substring(12,15).trim()
      ,arrKey(2) -> x.substring(15,16)
      ,arrKey(3) -> x.substring(16,17)
      ,arrKey(4) -> x.substring(17,25)
      ,arrKey(5) -> x.substring(25,34)
      ,arrKey(6) -> x.substring(34,46).trim()
      ,arrKey(7) -> x.substring(46,50)
      ,arrKey(8) -> x.substring(50,54)
      ,arrKey(9) -> x.substring(54,58)
      ,arrKey(10) -> x.substring(58,62)
      ,arrKey(11) -> x.substring(62,63)
      ,arrKey(12) -> x.substring(63,64)
      ,arrKey(13) -> x.substring(64,65)
      ,arrKey(14) -> x.substring(65,84).trim()
      ,arrKey(15) -> x.substring(84,85)
      ,arrKey(16) -> x.substring(85,88).trim()
      ,arrKey(17) -> x.substring(88,91).trim()
      ,arrKey(18) -> x.substring(91,94).trim()
      ,arrKey(19) -> x.substring(94,97).trim()
      ,arrKey(20) -> x.substring(17,34)))
    //.reduceByKeyAndWindow(_ + _, _ - _, Minutes(10), Seconds(2), 2)

    wordCounts.print()
    //wordCounts.foreachRDD( x => println(x.saveToEs("my/index")))
    //wordCounts.foreachRDD(rdd => rdd.saveToEs("LprData/LprData"))



    ssc.start()
    ssc.awaitTermination()
  }
}