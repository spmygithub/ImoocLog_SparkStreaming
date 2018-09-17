package cn.njupt.bigdata.spark.platform

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka.KafkaUtils

/**
  *   streaming对接kafka
  */
object StreamingPlatform {
  def main(args: Array[String]): Unit = {
    if(args.length != 4) {
      System.err.println("Usage: StreamingPlatform <zkQuorum> <group> <topics> <numThreads>")
      System.exit(1)
    }

    val Array(zkQuorum, group, topics, numThreads) = args

    val sparkConf = new SparkConf().setAppName("StreamingPlatform").setMaster("local[2]")

    val ssc = new StreamingContext(sparkConf, Seconds(5))

    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap

    // TODO... Spark Streaming如何对接Kafka
    val messages = KafkaUtils.createStream(ssc, zkQuorum, group,topicMap)

    // TODO... 测试该出的数据结构
    messages.map(_._2).count().print()

    ssc.start()
    ssc.awaitTermination()
  }
}

