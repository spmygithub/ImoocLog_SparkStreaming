# ImoocLog_SparkStreaming
慕课网日志实时处理SparkStreaming版

项目需求一：实时统计实战课程的访问量
项目需求一：实时统计从搜索引擎引流过来的实战课程访问量

项目流程：
1) 使用Python脚本generate_log.py并创建定时任务，每10秒产生一批数据 </br>
2）整合日志输出到flume </br>
3）整合flume到kafka </br>
4）整合kafka到spark streaming </br>
5）spark streaming对接收到的数据进行处理 </br>
项目主要完成了实时流数据平台的构建以及需求的开发，从数据的产生到对接flume，再从flume对接到kafka，再从kafka对接到streaming，再按照需求进行开发。
