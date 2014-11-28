package com.iflytek.utils

import java.sql.{ResultSet, DriverManager}

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Project Name:ISAPURL
 * Package Name:com.iflytek.utils
 * Date:14-11-27 下午2:38
 * Copyright (c) 2014, adleyyang.cn@gmail.com All Rights Reserved.
 */
object Constants {

  val categoryTransform = Map("constellation" -> "constellationDetail", "pm25" -> "airQuality")

  val ovsconn = "jdbc:mysql://192.168.86.16/ovs2?useUnicode=true&characterEncoding=utf8&user=ovs&password=ovs111"

  val isapurl_head = "http://kcloudtest.iflytek.com/service/isap?pageIndex=1&pageSize=10&appId=_testApp11111111&appKey=15b12297b8d1420685837e145c160b2a&sourceName="


  val ovsToNew = {
    val mongoClient = MongoClient("192.168.86.22")
    val collection = mongoClient("isap_web")("testurls")
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
      while (rs.next) {
        val BusinessName = rs.getString(1).split("_")(1)
        val NLU_Field = rs.getString(2)
        var OVS_Field = rs.getString(3)
        if (!OVS_Field.equals("NULL")) {
          ovsToNew.find(a =>a.get("service").toString.equals(BusinessName)) match {
            case Some(result) => //println(result)
            result.get(OVS_Field) match {
              case newfile :String => OVS_Field = newfile
              case _ =>
            }
            case None =>
          }

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
    finally {
      conn.close
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
