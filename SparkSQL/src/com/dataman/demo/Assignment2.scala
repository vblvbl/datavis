package com.dataman.demo

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.{StructType,StructField,StringType}
import com.sun.xml.internal.ws.wsdl.writer.document.Import
import org.apache.spark.sql.types.{DoubleType,LongType,BooleanType}
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._

object Assignment2 {
  val source = "hdfs://192.168.111.134:9000/Assignment2/jingdong.json"
  def main(args: Array[String]) = {
    System.setProperty("file.encoding", "UTF-8")
    val conf = new SparkConf().setAppName("assignment2")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val ds = sqlContext.read.json(source)
    val toDouble = udf[Double, String]( x => {
        if(x != null && x.toString().length() > 0 && x.substring(0,1) == "￥"){
          "%.1f".format(x.toString().substring(1).toDouble).toDouble
        }else{
          0.0
        }
      })
    val toInt = udf[Int, String]( x => {
          if(x != null && x.toString().length() > 0){
            x.toInt
          }else{
            0
          }
      })
      
    val df = ds.filter("type1=\"手机\"").withColumn("price", toDouble(ds("price"))).withColumn("AllComments", toInt(ds("AllComments")))
    df.registerTempTable("jingdong");
    val topComment = sqlContext.sql("SELECT itemId as c_itemId FROM jingdong order by AllComments desc limit 1000");
    val topPrice =  sqlContext.sql("SELECT itemId as p_itemId FROM jingdong order by price desc limit 1000");
    val tmp = topComment.join(topPrice, topComment.col("c_itemId").equalTo(topPrice("p_itemId")))
    val result = df.join(tmp, tmp.col("c_itemId").equalTo(df("itemId"))).select("model", "price","AllComments","HighPraise","itemId","brand")
    result.collect().foreach {
      x=>{
        print('{')
        print("\"model\":\""+x.getString(0)+"\",")
        print("\"price\":\""+x.getDouble(1)+"\",")
        print("\"AllComments\":\""+x.getInt(2)+"\",")
        print("\"HighPraise\":\""+x.getString(3)+"\",")
        print("\"itemId\":\""+x.getString(4)+"\",")
        print("\"brand\":\""+x.getString(5)+"\"")
        println('}')
      } 
      
    }
    sc.stop()
  }
}
