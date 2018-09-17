package cn.njupt.bigdata.spark.project.spark

import cn.njupt.bigdata.spark.project.dao.{CourseClickCountDAO, CourseSearchClickCountDAO}
import cn.njupt.bigdata.spark.project.domain.{ClickLog, CourseClickCount, CourseSearchClickCount}
import cn.njupt.bigdata.spark.project.utils.DateUtils
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka.KafkaUtils

import scala.collection.mutable.ListBuffer

/**
  *   streaming对接kafka过来的数据
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

    // TODO... 测试数据接收
    messages.map(_._2).count().print()

    //TODO.... 数据清洗
    val logs = messages.map(_._2)
    val cleanData = logs.map(line => {
      val infos = line.split("\t")

      // infos(2) = "GET /class/130.html HTTP/1.1"
      // url = /class/130.html
      val url = infos(2).split(" ")(1)
      var courseId = 0

      //把实战课程的课程编号拿到
      if(url.startsWith("/class")){
        val courseIdHTML = url.split("/")(2)
        courseId = courseIdHTML.substring(0,courseIdHTML.lastIndexOf(".")).toInt
      }

      ClickLog(infos(0),DateUtils.parse(infos(1)),courseId,infos(3).toInt,infos(4))
    }).filter(clicklog => clicklog.courseId != 0)


    //cleanData.print()

    //统计到今天为止实战课程的访问量
    cleanData.map(x => {
      //HBase rowkey设计： 20171111_10
      (x.time.substring(0,8) + "_" + x.courseId,1)
    }).reduceByKey(_+_).foreachRDD(rdd => {
      rdd.foreachPartition( partitionRecords => {
        val list = new ListBuffer[CourseClickCount]
        partitionRecords.foreach(pair => {
          list.append(CourseClickCount(pair._1,pair._2))
          })
        CourseClickCountDAO.save(list)
      })
    })


    //统计到今天为止从搜索引擎引流过来的实战课程的访问量
    cleanData.map(x => {
      //https://www.sogou.com/web?query=Spark SQL实战 ==> https:/www.sogou.com/web?query=Spark SQL实战
      val referer = x.referer.replaceAll("//","/")
      val splits = referer.split("/")
      var host = ""
      if (splits.length >2){
        host = splits(1)
      }
      (host,x.courseId,x.time)
    }).filter(_._1 != "").map(x => {
      (x._3.substring(0,8) + "_" + x._1 + "_" + x._2, 1)
    }).reduceByKey(_+_).foreachRDD(rdd => {
      rdd.foreachPartition( partitionRecords => {
        val list = new ListBuffer[CourseSearchClickCount]
        partitionRecords.foreach(pair => {
          list.append(CourseSearchClickCount(pair._1,pair._2))
        })
        CourseSearchClickCountDAO.save(list)
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
