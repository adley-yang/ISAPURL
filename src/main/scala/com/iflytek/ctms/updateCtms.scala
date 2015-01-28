package com.iflytek.ctms

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoDB, MongoClient}
import org.bson.types.ObjectId
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.native.JsonMethods._
import com.mongodb.casbah.Imports._
import scala.collection._

/**
 * Project Name:ISAPURL
 * Package Name:com.iflytek.ctms
 * Date:15-1-27 上午11:39
 * Copyright (c) 2015, adleyyang.cn@gmail.com All Rights Reserved.
 */
object updateCtms extends App {

  implicit val formats = DefaultFormats

  val processServices = Set("flight", "flightBooking", "renting", "shortRent", "weibo", "microblog", "gift", "flower", "cake", "motorViolation", "illegal")

  val tempMap = mutable.Map.empty[String, Map[String, List[String]]]

  val tempSMap = mutable.Map.empty[(String, String), Map[String, List[String]]]

  val mongoClient = MongoClient("192.168.86.22")
  val collection = mongoClient("test2")("clients")

  val query = MongoDBObject("env"->"onlinepre","appid" -> "openapi*".r)

  //val query = MongoDBObject("_id" -> new ObjectId("52d3a08c4f902f13408b54d7"))
  val allDocs = collection.find(query)

  //println(allDocs.count)
  //System.exit(0)

  for (doc <- allDocs) {
    val id = doc.get("_id") toString
    val services = doc.get("services")
    val scenes = doc.get("scenes")
    /*业务更新*/
    if (services != null) {
      val serviceJson = parse(services toString)
      val serviceMap = serviceJson.values.asInstanceOf[Map[String, List[String]]]
      for ((k, v) <- serviceMap) {
        if (processServices.contains(k)) {
          tempMap get (id) match {
            case Some(result) =>
            case None => tempMap += (id -> serviceMap)
          }
        }
      }
    }
    /*场景更新*/
    if (scenes != null && !scenes.toString.equals("{ }")) {
      val sceneJson = parse(scenes toString)

      val sceneMap = sceneJson.values.asInstanceOf[Map[String, Map[String, Map[String, List[String]]]]]
      for ((sceneName, tMap) <- sceneMap) {
        val sTMap = tMap.get("scene") match {
          case Some(result) => result
          case None => Map.empty[String, List[String]]
        }
        val tup = (id, sceneName)
        for ((k, v) <- sTMap) {

          if (processServices.contains(k)) {
            tempSMap get (tup) match {
              case Some(result) =>
              case None =>
                tempSMap += (tup -> sTMap)
            }
          }
        }
      }
    }
  }

  /*service update*/
  for ((id, serviceMap) <- tempMap) {

    println("start:" + id + "-" + serviceMap)

    val updateService = mutable.Map.empty[String, List[String]]
    for ((k, v) <- serviceMap) updateService += (k -> v)

    /*flight 机票查询  -- 吞并  flightBooking 机票预订*/
    if (updateService.contains("flightBooking")) {
      val list1 = updateService.get("flightBooking") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("flight") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      updateService -= ("flightBooking")
      updateService += ("flight" -> mergeList(list1, list2))
    }

    /*renting    -- 合并renting租房，  shortRent 短租房*/
    if (updateService.contains("shortRent")) {
      val list1 = updateService.get("renting") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("shortRent") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      updateService -= ("shortRent")
      updateService += ("renting" -> mergeList(list1, list2))
    }

    /*weibo     --  合并weibo微博，  microblog 发微博*/
    if (updateService.contains("microblog")) {
      val list1 = updateService.get("weibo") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("microblog") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      updateService -= ("microblog")
      updateService += ("weibo" -> mergeList(list1, list2))
    }

    /*gift （订购礼品）、flower(订购鲜花)、cake(订购蛋糕)   合并为   giftX(订购礼品) ；*/
    if (updateService.contains("gift") || updateService.contains("flower") || updateService.contains("cake")) {
      val list1 = updateService.get("gift") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("flower") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list3 = updateService.get("cake") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      updateService -= ("gift")
      updateService -= ("flower")
      updateService -= ("cake")
      updateService += ("giftX" -> mergeList(list1, list2, list3))
    }

    /*motorViolation (机动车违章) 、illegal(违章查询)   合并为   illegalX(违章查询)；*/
    if (updateService.contains("motorViolation") || updateService.contains("illegal")) {
      val list1 = updateService.get("motorViolation") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("illegal") match {
        case Some(result) => result
        case None => List.empty[String]
      }

      updateService -= ("motorViolation")
      updateService -= ("illegal")
      updateService += ("illegalX" -> mergeList(list1, list2))
    }
    println("end:" + id + "-" + updateService)

    val query = MongoDBObject("_id" -> new ObjectId(id),"env"->"test")
    val update = $set("services" -> updateService)
    collection.update(query, update)
  }


  /*scene update*/
  for ((id, serviceMap) <- tempSMap) {

    println("start:" + id + "-" + serviceMap)

    val updateService = mutable.Map.empty[String, List[String]]
    for ((k, v) <- serviceMap) updateService += (k -> v)

    /*flight 机票查询  -- 吞并  flightBooking 机票预订*/
    if (updateService.contains("flightBooking")) {
      val list1 = updateService.get("flightBooking") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("flight") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      updateService -= ("flightBooking")
      updateService += ("flight" -> mergeList(list1, list2))
    }

    /*renting    -- 合并renting租房，  shortRent 短租房*/
    if (updateService.contains("shortRent")) {
      val list1 = updateService.get("renting") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("shortRent") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      updateService -= ("shortRent")
      updateService += ("renting" -> mergeList(list1, list2))
    }

    /*weibo     --  合并weibo微博，  microblog 发微博*/
    if (updateService.contains("microblog")) {
      val list1 = updateService.get("weibo") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("microblog") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      updateService -= ("microblog")
      updateService += ("weibo" -> mergeList(list1, list2))
    }

    /*gift （订购礼品）、flower(订购鲜花)、cake(订购蛋糕)   合并为   giftX(订购礼品) ；*/
    if (updateService.contains("gift") || updateService.contains("flower") || updateService.contains("cake")) {
      val list1 = updateService.get("gift") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("flower") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list3 = updateService.get("cake") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      updateService -= ("gift")
      updateService -= ("flower")
      updateService -= ("cake")
      updateService += ("giftX" -> mergeList(list1, list2, list3))
    }

    /*motorViolation (机动车违章) 、illegal(违章查询)   合并为   illegalX(违章查询)；*/
    if (updateService.contains("motorViolation") || updateService.contains("illegal")) {
      val list1 = updateService.get("motorViolation") match {
        case Some(result) => result
        case None => List.empty[String]
      }
      val list2 = updateService.get("illegal") match {
        case Some(result) => result
        case None => List.empty[String]
      }

      updateService -= ("motorViolation")
      updateService -= ("illegal")
      updateService += ("illegalX" -> mergeList(list1, list2))
    }
    println("end:" + id + "-" + updateService)

    val query = MongoDBObject("_id" -> new ObjectId(id._1))
    val key = "scenes." + id._2 + ".scene"
    val update = $set(key -> updateService)
    collection.update(query, update)
  }


  def mergeList(lists: List[String]*): List[String] = {
    val set = mutable.HashSet.empty[String]
    for (list <- lists) {
      for (elem <- list) set += elem
    }
    set.toList
  }
}
