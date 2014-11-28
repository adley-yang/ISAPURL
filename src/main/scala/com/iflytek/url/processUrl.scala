package com.iflytek.url

import java.io.PrintWriter

import com.iflytek.utils.Constants
import org.json4s.{DefaultFormats, _}
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._

import scala.collection.mutable
import scala.io.Source

/**
 * Project Name:ISAPURL
 * Package Name:com.iflytek.url
 * Date:14-11-27 下午2:37
 * Copyright (c) 2014, adleyyang.cn@gmail.com All Rights Reserved.
 */
object processUrl {

  def main(args: Array[String]) {
    println("isap url转换工具")
    if (args.length != 2) {
      println("配置：source_file url_file")
      System.exit(0)
    }

    implicit val formats = DefaultFormats

    val isap_source = Source.fromFile(args(0), "UTF-8")
    val isap_url = new PrintWriter(args(1), "UTF-8")
    try {
      for (line <- isap_source.getLines()) {
        val data = line.split("\\t")
        val service = data(0)
        val text = data(1)
        val solts = compact(render(parse(data(2)) \ "slots"))
        val solts_obj = parse(data(2)) \ "slots"

        val ovsMap = Constants.nluToOvs.get(service) match {
          case Some(result) => result
          case None => Map()
        }
        val solts_map = mutable.Map[String, String]()
        for ((k, v) <- ovsMap) {
          if (!k.contains(".")) {
            val k_value = solts_obj \ k
            if (k_value != JNothing) {
              solts_map += (v -> k_value.extract[String])
            }
          } else {
            val k1 = k.split("\\.")(0)
            val k2 = k.split("\\.")(1)
            val k_value = solts_obj \ k1 \ k2
            if (k_value != JNothing) {
              solts_map += (v -> k_value.extract[String])
            }
          }
        }

        if (solts_map.size > 0) {

          val solts_new = write(solts_map)
          //service => category
          val category = Constants.categoryTransform.get(service) match {
            case Some(result) => result toString()
            case None => service
          }

          //category => sourceName : Buffer[String]
          val sourceName: mutable.Buffer[String] = Constants.categoryToSourceName.get(category) match {
            case Some(result) => result
            case None => mutable.Buffer()
          }
          for (sName <- sourceName) {
            val url = Constants.isapurl_head + sName + "&q=" + solts_new
            isap_url.println(service + "\t" + text + "\t" + solts + "\t" + solts_new + "\t" + url)
          }
        }
      }
    } finally {
      isap_source.close();
      isap_url.close();
    }
  }
}
