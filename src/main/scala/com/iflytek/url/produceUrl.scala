package com.iflytek.url

import java.io.FileWriter
import java.net.URLEncoder

import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.native.JsonMethods._
import scala.io.Source
import scalaj.http.{HttpOptions, Http}

/**
 * Project Name:ISAPURL
 * Package Name:com.iflytek.url
 * Date:15-1-22 下午2:10
 * Copyright (c) 2015, adleyyang.cn@gmail.com All Rights Reserved.
 */
object produceUrl extends App {

  implicit val formats = DefaultFormats

  val services = Set("takeout", "parenting", "easyMoment", "homeInfo", "lifeTips", "constellationMatching", "mimi", "englishWords", "travelCity", "insurance", "openClass", "tingshu", "clothing", "children", "restaurant", "magazine", "train", "fund", "travelX", "danceTeaching", "composition", "smartName", "hotel", "drugstore", "zodiacFortune", "qiubai", "secondaryTransaction", "english", "exam", "fm")

  val testHead = "http://kcloudtest.iflytek.com/service/iss?ver=2.0&method=query&appid=kctestisap01&usr_id=kctestisap01&addr.city=%E5%90%88%E8%82%A5%E5%B8%82&addr.pos=%E5%AE%89%E5%BE%BD%E7%9C%81%E5%90%88%E8%82%A5%E5%B8%82%E8%82%A5%E8%A5%BF%E5%8E%BF%E6%9C%9B%E6%B1%9F%E8%A5%BF%E8%B7%AF666%E5%8F%B7&addr.street=%E6%9C%9B%E6%B1%9F%E8%A5%BF%E8%B7%AF&text="

  val onlineHead = "http://kchf.openspeech.cn/service/iss?ver=2.0&method=query&appid=kctestisap01&usr_id=kctestisap01&addr.city=%E5%90%88%E8%82%A5%E5%B8%82&addr.pos=%E5%AE%89%E5%BE%BD%E7%9C%81%E5%90%88%E8%82%A5%E5%B8%82%E8%82%A5%E8%A5%BF%E5%8E%BF%E6%9C%9B%E6%B1%9F%E8%A5%BF%E8%B7%AF666%E5%8F%B7&addr.street=%E6%9C%9B%E6%B1%9F%E8%A5%BF%E8%B7%AF&flag=test&text="

  val sFile = Source.fromFile("D:\\isap.txt", "UTF-8").getLines()
  scala.io.Source

  val fileName = "C:\\isap\\all.txt"
  val file = new java.io.File(fileName)
  if (!file.exists()) {
    file.createNewFile()
  }


  def processFile(fileName: String, content: String): Unit = {
    val file = new java.io.File(fileName)
    if (!file.exists()) {
      file.createNewFile()
    }
    appendFile(fileName, content)
  }

  /**
   * 追加文件：使用FileWriter
   *
   * @param fileName
   * @param content
   */
  def appendFile(fileName: String, content: String) {
    try {
      // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
      val writer = new FileWriter(fileName, true);
      writer.write(content);
      writer.close();
    } catch {
      case e: Exception => println(e)
    }
  }

  def getService(url: String) = {
    val json = Http.get(url).options(HttpOptions.connTimeout(5000), HttpOptions.readTimeout(5000)).asString
    val service: String = try {
      parse(json) \ "service" match {
        case str: JString => str.extract[String]
      }
    } catch {
      case e: Exception => ""
    }
    service
  }

  for (sLine <- sFile) {
    try {
      val text = sLine.split("\t")(0)
      val ectext = URLEncoder.encode(text, "UTF-8")
      val service = sLine.split("\t")(1)

      if (services.contains(service)) {

        val testUrl = testHead + ectext + "&category=" + service
        val onlineUrl = onlineHead + ectext + "&category=" + service
        val content = text + "\t" + service + "\t" + testUrl + "\t" + onlineUrl + "\n"

        var onlineService: String = getService(onlineUrl)
        if (onlineService == "") {
          onlineService = getService(onlineUrl)
          if (onlineService == "") {
            onlineService = getService(onlineUrl)
          }
        }
        var testService = getService(testUrl)
        if (testService == "") {
          testService = getService(testUrl)
          if (testService == "") {
            testService = getService(testUrl)
          }
        }

        if (onlineService.equals(service) && testService.equals(service)) {
          val fileName = "C:\\isap_ok\\" + service + ".txt"
          val file = new java.io.File(fileName)
          processFile(fileName, content)
        } else if (onlineService.equals(service) && !testService.equals(service)) {
          val fileName = "C:\\isap_failed\\" + service + ".txt"
          val file = new java.io.File(fileName)
          processFile(fileName, content)
        } else {
          val fileName = "C:\\isap_exception\\" + service + ".txt"
          val file = new java.io.File(fileName)
          processFile(fileName, content)
        }
      }
    } catch {
      case e: Exception => println(e) + "\r\n" + sLine
    }
  }
}
