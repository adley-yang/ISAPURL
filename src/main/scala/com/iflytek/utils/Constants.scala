package com.iflytek.utils

import java.sql.{ResultSet, DriverManager}
import java.text.SimpleDateFormat

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject

import scala.collection.mutable

/**
 * Project Name:ISAPURL
 * Package Name:com.iflytek.utils
 * Date:14-11-27 下午2:38
 * Copyright (c) 2014, adleyyang.cn@gmail.com All Rights Reserved.
 */
object Constants {
  val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
  val format_date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val format_time = new java.text.SimpleDateFormat("HH:mm:ss")
  //val categoryTransform = Map("constellation" -> "constellationDetail", "pm25" -> "airQuality")

  val ovsconn = "jdbc:mysql://192.168.86.16/ovs2?useUnicode=true&characterEncoding=utf8&user=ovs&password=ovs111"

  //"http://kcloudtest.iflytek.com/service/isap?pageIndex=1&pageSize=10&appid=wardTest&appKey=eac56c72ffb04f68b5f57dd0b099836a&category=travelBlogs&q=%7B%22*%22:%22*%22%7D"
  val isapurl_head = "http://kcloudtest.iflytek.com/service/isap?pageIndex=1&pageSize=10&appid=wardTest&appKey=eac56c72ffb04f68b5f57dd0b099836a&category="

  val categoryTransform: scala.collection.mutable.Map[String, String] = {
    val tmp = scala.collection.mutable.Map[String, String]()
    val mongoClient = MongoClient("192.168.86.22")
    val collection = mongoClient("isap_web")("fieldTransfer")
    val allDocs = collection.find()
    for (doc <- allDocs) {
      val ovsService = doc.get("ovsService")
      val isapService = doc.get("isapService")
      if (ovsService != null && isapService != null) {
        val a = ovsService.toString
        val b = isapService.toString
        tmp += (a -> b)
      }

    }
    mongoClient.close()
    tmp
  }


  val ovsToNew = {
    val mongoClient = MongoClient("192.168.86.22")
    val collection = mongoClient("isap_web")("fieldTransfer")
    val results = collection.find().toList
    mongoClient.close()
    results
  }


  //获取mysql字段映射
  classOf[com.mysql.jdbc.Driver]
  val nluToOvs: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, String]] = {
    val tmp = scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, String]]()
    // Setup the connection
    val conn = DriverManager.getConnection(Constants.ovsconn)
    try {
      // Configure to be Read Only
      val statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
      // Execute Query
      val rs = statement.executeQuery("SELECT BusinessName,NLU_Field,OVS_Field FROM `nlufieldmap` ORDER BY BusinessName")
      // Iterate Over ResultSet
      val mysqlService: mutable.Buffer[String] = mutable.Buffer.empty
      while (rs.next) {
        val BusinessName = rs.getString(1).split("_")(1)
        mysqlService += BusinessName
        val NLU_Field = rs.getString(2)
        var OVS_Field = rs.getString(3)
        if (!OVS_Field.equals("NULL")) {
          val flag = ovsToNew.find(a => a.get("ovsService").toString.equals(BusinessName)) match {
            case Some(result) => //println(result)
              result.get(OVS_Field) match {
                case newfile: String => OVS_Field = newfile
                  true
                case _ => true
              }
            case None => println("not isap:" + BusinessName)
              false
          }
          if (flag == true) {
            tmp.get(BusinessName) match {
              case Some(result) =>
                result += (NLU_Field -> OVS_Field)
                tmp += (BusinessName -> result)
              case None =>
                tmp += (BusinessName -> scala.collection.mutable.Map(NLU_Field -> OVS_Field))
            }
          }
        }
      }
}
    finally {
      conn.close
    }

    val list = List("ringDiy", "app", "music", "voice", "riddle", "qiubai", "constellation", "baozou", "picture", "video", "cinemas", "weather")
    for (x : String <- list) {
      if(!tmp.contains(x)){

        val newmap = ovsToNew.find(a=>a.get("ovsService").toString.equals(x)) match {
          case Some(result) =>
            val tmpnew = scala.collection.mutable.Map[String, String]()
            val results = result.keySet()
            for(newkey  <- results toArray()){
              val news = newkey match {
                case a : String => a
                case _ => ""
              }
              tmpnew += (news -> result.get(news).toString)
            }
            tmpnew
          case None => mutable.Map.empty[String,String]
        }
        tmp += (x -> newmap)
      }
    }
    tmp
  }




  val categoryToSourceName: scala.collection.mutable.Map[String, scala.collection.mutable.Buffer[String]] = {
    val mongoClient = MongoClient("192.168.86.22")
    val collection = mongoClient("isap_web")("info_source")
    val tmp = scala.collection.mutable.Map[String, scala.collection.mutable.Buffer[String]]()
    val status = MongoDBObject("status" -> "ONLINE")
    val allDocs = collection.find(status)
    for (doc <- allDocs) {
      val category: String = doc.get("category").toString
      val sourceName = doc.get("sourceName").toString
      tmp.get(category) match {
        case Some(result) => result += sourceName
          tmp += (category -> result)
        case None => tmp += (category -> scala.collection.mutable.Buffer(sourceName))
      }
    }
    mongoClient.close()
    tmp
  }
}
