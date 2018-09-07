package cn.njupt.bigdata.spark

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  *   Spark Streaming处理文件系统（HDFS/File）数据
  */
object HdfsWordCount {
  // 创建StreamingContext需要两个参数：SparkConf和batch interval
  // Create the context with a 1 second batch size
  val sparkConf = new SparkConf().setAppName("HdfsWordCount").setMaster("local[2]")
  val ssc = new StreamingContext(sparkConf, Seconds(3))

  // Create the FileInputDStream on the directory and use the
  // stream to count words in new files created
  val lines = ssc.textFileStream("file:///opt/spark/data/") //输入监控的目录
  val words = lines.flatMap(_.split(" "))
  val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
  wordCounts.print()
  ssc.start()
  ssc.awaitTermination()
}
