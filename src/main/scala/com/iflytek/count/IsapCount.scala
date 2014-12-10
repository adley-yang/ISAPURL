package com.iflytek.count

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoDB, MongoClient}

import scala.collection.mutable
import scala.collection.mutable._

/**
 * Project Name:ISAPURL
 * Package Name:com.iflytek.count
 * Date:14-12-2 下午6:43
 * Copyright (c) 2014, adleyyang.cn@gmail.com All Rights Reserved.
 */
object IsapCount extends App {

  val mapCount = Map[String, Int]()

  val isapCount = mutable.LinkedHashMap[String, Int]()

  val mongoClient = MongoClient("192.168.86.22")
  val db: MongoDB = mongoClient("isap_data")
  val colNames = db.getCollectionNames()
  for (colName <- colNames) {
    val collection = db(colName)
    val count = collection.count()
    mapCount += (colName -> count)
  }
  isapCount += ("1-1W" -> (mapCount.filter(x => x._2 >= 1 && x._2 <= 10000)).size)
  isapCount += ("1W-10W" -> (mapCount.filter(x => x._2 > 10000 && x._2 <= 100000)).size)
  isapCount += ("10W-50W" -> (mapCount.filter(x => x._2 > 100000 && x._2 <= 500000)).size)
  isapCount += ("50W-100W" -> (mapCount.filter(x => x._2 > 500000 && x._2 <= 1000000)).size)
  isapCount += ("100W-300W" -> (mapCount.filter(x => x._2 > 1000000 && x._2 <= 3000000)).size)
  isapCount += ("300W- +oo" -> (mapCount.filter(x => x._2 > 3000000)).size)
  for ((k, v) <- isapCount) {
    println(k + "\t" + v)
  }

  val buf = Buffer[String]()
  val collection = mongoClient("isap_web")("info_source")
  val allDocs = collection.find()

  for (doc <- allDocs) {
    val sourceName = doc.get("sourceName")
    val updateType = doc.get("updateType")
    buf += (updateType toString)
  }

  val result = buf.map(x => (x, 1)).groupBy(_._1).map {
    case (which, counts) => (which, counts.foldLeft(0)(_ + _._2))
  }

  println("------------------------------------------------------------------")

  for ((k, v) <- result) {
    println(k + "\t" + v)
  }

  val updateType = MongoDBObject("updateType" -> "MINUTE")
  val maxDocs = collection.find(updateType)
  var tmp_total = 0
  var tmp_name = ""
  for (doc <- maxDocs) {
    val sourceName = doc.get("sourceName") toString
    val total = try {
      doc.get("total").toString.toInt
    } catch {
      case e: Exception => 0
    }
    if (total > tmp_total) tmp_total = total; tmp_name = sourceName
  }
  println(s"sourceName:${tmp_name}  total:${tmp_total}")

  println("------------------------------------------------------------------")

  val updateTypeday = MongoDBObject("updateType" -> "DAY")
  val maxDocsday = collection.find(updateTypeday)
  var tmp_totalday = 0
  var tmp_nameday = ""
  for (doc <- maxDocsday) {
    val sourceName = doc.get("sourceName") toString
    val total = try {
      doc.get("total").toString.toInt
    } catch {
      case e: Exception => 0
    }
    if (total > tmp_totalday) tmp_totalday = total; tmp_nameday = sourceName
  }
  println(s"sourceName:${tmp_nameday}  total:${tmp_totalday}")

  mongoClient.close()
}
