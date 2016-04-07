
package com.dataman.demo

import org.apache.log4j.{ Level, Logger }
import org.apache.spark.{ SparkContext, SparkConf }
import org.apache.spark.rdd.PairRDDFunctions
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import org.json4s._
import org.apache.spark.Accumulator


object Assignment1 {
  def main(args: Array[String]) = {
    val conf = new SparkConf().setAppName("assignment1").setMaster("local")
    val sc = new SparkContext(conf)
    Logger.getRootLogger.setLevel(Level.WARN)
    //please code here. 
    //val words = sc.textFile("hdfs://192.168.111.134:9000/Assignment1")
    val words = sc.textFile("file:///Users/rmk/Downloads/abc.txt")
    .map(file=>file.split(","))
    .map(item =>{
      (item(0),item(1))
    })
    .flatMap(file => {
      var map = Map[String,ArrayBuffer[Tuple2[String,String]]]()
      val words = file._2.split(" ").iterator
      val doc = file._1
      var counter = -1
      while(words.hasNext){
        val word = words.next()
        counter +=1
        val pos = counter.toString()
        if(map.contains(word)){
          map(word) += Tuple2(doc,pos)
        }else{
          map += (word->ArrayBuffer(Tuple2(doc,pos)))
        }
      }
      map
    })
    
    val result = words.aggregateByKey(Map[String,ArrayBuffer[String]]())((k,v)=>{
      val itr = v.iterator
      while(itr.hasNext){
        val vv = itr.next()
        if(k contains vv._1){
          k(vv._1) += vv._2
        }else{
          k += (vv._1->ArrayBuffer(vv._2))
        }
      }
      k
    },(u1,u2)=>{
      val itr = u2.iterator
      while(itr.hasNext){
        val u2map = itr.next()
        if(u1 contains u2map._1){
          u1(u2map._1)++=u2map._2
        }else{
          u1(u2map._1) = u2map._2
        }
      }
      u1
    })
    .filter(map=>{
      var count = 0
      val mapitr = map._2.iterator
      while(mapitr.hasNext){
        mapitr.next()
        count +=1
      }
      if(count == 1){
        true
      }else{
        false
      }
     })
     val resultitr = result.toLocalIterator
     while(resultitr.hasNext){
      val map = resultitr.next()
      print("{\""+map._1+"\":[")
      val mapitr = map._2.iterator
      while(mapitr.hasNext){
        val maplist = mapitr.next()
        print("{\""+maplist._1+"\":[")
        val positr = maplist._2.iterator
        while(positr.hasNext){
          val pos = positr.next()
          print(pos)
          if(positr.hasNext){
            print(",")
          }
        }
        print("]}")
        if(mapitr.hasNext){
            print(", ")
        }
      }
      print("]}")
      println()
     }
    sc.stop()
  }
}
