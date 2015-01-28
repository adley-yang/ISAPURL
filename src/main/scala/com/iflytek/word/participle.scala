package com.iflytek.word

import java.io.PrintWriter

import com.iflytek.wordsegmenter.Iflyws
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoDB}
import org.json4s.native.JsonMethods._
import org.json4s.{DefaultFormats, _}

import scala.collection.mutable

/**
 * Project Name:ISAPURL
 * Package Name:com.iflytek.word
 * Date:14-12-8 下午3:08
 * Copyright (c) 2014, adleyyang.cn@gmail.com All Rights Reserved.
 */
object participle extends App {
  if (args.length != 1) {
    println("配置：file_path")
    System.exit(0)
  }
  implicit val formats = DefaultFormats
  val iflyws = new Iflyws()
  val isap_participle = new PrintWriter(args(0), "UTF-8")
  val mongoClient = MongoClient("192.168.86.22")
  val db: MongoDB = mongoClient("isap_data")
  val db_web = mongoClient("isap_web")

  val col_info_sou = db_web("info_source")
  val col_category = db_web("category")
  val status = MongoDBObject("status" -> "ONLINE")
  val info_all = col_info_sou.find(status) toList
  val category_all = col_category.find() toList

  //val colNames = db.getCollectionNames().filterNot(x => x.startsWith("_test"))
  val colNames = List("coolRinger","gameApp","selfMusic","himalayan","riddle","coldJoke","constellationDetail","baozouCartoon","internet","youkuFilm","catEyeFilm","zhiquWeather")
    try {
    for (colName <- colNames) {
      val category = info_all.find(x => x.get("sourceName").toString.equals(colName)) match {
        case Some(result) => result.get("category")
        case None => None
      }
      if (category != None) {
        val dataItems = category_all.find(x => x.get("catName").equals(category)) match {
          case Some(result) => val dataItems = result.get("dataItems")
            val list = parse(dataItems toString).children
            val buf = mutable.Buffer[String]()
            for (items <- list) {
              val isIndex = items \ "isIndex" match {
                case result: JBool => result.extract[Boolean]
                case _ => false
              }
              val isToken = items \ "isToken" match {
                case result: JBool => result.extract[Boolean]
                case _ => false
              }
              if (isIndex == true && isToken == true) items \ "itemName" match {
                case result: JValue =>
                  val itemName = result.extract[String]
                  val allDocs = db(colName).find()
                  for (doc <- allDocs) {
                    val value = doc.get(itemName) match {
                      case result: String =>
                        if (result.length > 0) {
                          val ovs_result = ovs(result toString)
                          isap_participle.println(category + "\t" + colName + "\t" + result + "\t" + ovs_result)
                        }
                      case _ =>
                    }
                  }
                case _ => ""
              }
            }
            buf
          case None => None
        }
      }
    }
  } finally {
    isap_participle.close()
  }

  def ovs(source: String): String = {
    try {
      iflyws.getTokenString(source)
    } catch {
      case e: Exception => println("原始串"+source+","+e)
        ""
    }
  }

}
