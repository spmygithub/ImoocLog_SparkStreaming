package cn.njupt.bigdata.spark

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  *   Spark Streaming处理Socket数据
  *   测试：nc -lk 6789
  */
object NetworkWordCount {
  // 创建StreamingContext需要两个参数：SparkConf和batch interval
  // Create the context with a 1 second batch size
  val sparkConf = new SparkConf().setAppName("NetworkWordCount").setMaster("local[2]")
  val ssc = new StreamingContext(sparkConf, Seconds(1))

  // Create a socket stream on target ip:port and count the
  // words in input stream of \n delimited text (eg. generated by 'nc')
  // Note that no duplication in storage level only for running locally.
  // Replication necessary in distributed scenario for fault tolerance.
  val lines = ssc.socketTextStream("localhost",6789)
  val words = lines.flatMap(_.split(" "))
  val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
  wordCounts.print()
  ssc.start()
  ssc.awaitTermination()
}