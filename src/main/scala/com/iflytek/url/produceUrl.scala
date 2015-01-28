package com.iflytek.url

import java.io.{FileOutputStream, FileWriter}
import java.net.URLEncoder

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.native.JsonMethods._
import scala.io.Source
import scalaj.http.{HttpOptions, Http}
import scala.collection._

/**
 * Project Name:ISAPURL
 * Package Name:com.iflytek.url
 * Date:15-1-22 下午2:10
 * Copyright (c) 2015, adleyyang.cn@gmail.com All Rights Reserved.
 */
object produceUrl extends App {

  if (args.length != 1) {
    println("配置：source_file")
    System.exit(0)
  }

  implicit val formats = DefaultFormats

  val services = Set("takeout", "parenting", "easyMoment", "homeInfo", "lifeTips", "constellationMatching", "mimi", "englishWords", "travelCity", "insurance", "openClass", "tingshu", "clothing", "children", "restaurant", "magazine", "train", "fund", "travelX", "danceTeaching", "composition", "smartName", "hotel", "drugstore", "zodiacFortune", "qiubai", "secondaryTransaction", "english", "exam", "fm")

  val testHead = "http://kcloudtest.iflytek.com/service/iss?ver=2.0&method=query&rt=semantic|data|webPage&appid=kctestisap01&usr_id=kctestisap01&addr.city=%E5%90%88%E8%82%A5%E5%B8%82&addr.pos=%E5%AE%89%E5%BE%BD%E7%9C%81%E5%90%88%E8%82%A5%E5%B8%82%E8%82%A5%E8%A5%BF%E5%8E%BF%E6%9C%9B%E6%B1%9F%E8%A5%BF%E8%B7%AF666%E5%8F%B7&addr.street=%E6%9C%9B%E6%B1%9F%E8%A5%BF%E8%B7%AF&text="

  val onlineHead = "http://kchf.openspeech.cn/service/iss?ver=2.0&method=query&rt=semantic|data|webPage&appid=kctestisap01&usr_id=kctestisap01&addr.city=%E5%90%88%E8%82%A5%E5%B8%82&addr.pos=%E5%AE%89%E5%BE%BD%E7%9C%81%E5%90%88%E8%82%A5%E5%B8%82%E8%82%A5%E8%A5%BF%E5%8E%BF%E6%9C%9B%E6%B1%9F%E8%A5%BF%E8%B7%AF666%E5%8F%B7&addr.street=%E6%9C%9B%E6%B1%9F%E8%A5%BF%E8%B7%AF&flag=test&text="

  val excelMap: mutable.HashMap[String, mutable.Buffer[(String, String, String, String, String, String, String, String, String, String)]] = mutable.HashMap.empty[String, mutable.Buffer[(String, String, String, String, String, String, String, String, String, String)]]

  //val sFile = Source.fromFile("D:\\isap_tmp.txt", "UTF-8").getLines()
  val sFile = Source.fromFile(args(0), "UTF-8").getLines()

  //创建excel工作簿
  val wb = new HSSFWorkbook()

  // 创建字体样式
  val font = wb.createFont();
  font.setColor(HSSFColor.RED.index);


  // 创建单元格样式
  val style = wb.createCellStyle()
  style.setFillForegroundColor(HSSFColor.RED.index)
  style.setFillBackgroundColor(HSSFColor.RED.index)
  style.setFont(font)


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
    val semantic: String = try {
      compact(render(parse(json) \ "semantic"))
    } catch {
      case e: Exception => ""
    }

    val data: String = try {
      compact(render(parse(json) \ "data" \ "result"))
    } catch {
      case e: Exception => ""
    }

    val resultFlag = if (data.length > 0) "true" else "false"

    (service, semantic, resultFlag)
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

        var onlinetup = getService(onlineUrl)

        if (onlinetup._1.equals("")) {
          onlinetup = getService(onlineUrl)
          if (onlinetup._1.equals("")) {
            onlinetup = getService(onlineUrl)
          }
        }
        val onlineService = onlinetup._1
        val onlineSemantic = onlinetup._2
        val onlineResult = onlinetup._3


        var testtup = getService(testUrl)

        if (testtup._1.equals("")) {
          testtup = getService(testUrl)
          if (testtup._1.equals("")) {
            testtup = getService(testUrl)
          }
        }
        val testService = testtup._1
        val testSemantic = testtup._2
        val testResult = testtup._3

        val temptup = (text, service, onlineService, testService, onlineResult, testResult, onlineSemantic, testSemantic, onlineUrl, testUrl)
        excelMap.get(service) match {
          case Some(result) =>
            result += temptup
            excelMap += (service -> result)
          case None => excelMap += (service -> mutable.Buffer(temptup))
        }

        println(service+"---"+text)


        /*if (onlineService.equals(service) && testService.equals(service)) {
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
        }*/
      }
    } catch {
      case e: Exception => println(e) + "\r\n" + sLine
    }
  }
  println("done url work")
  for ((k, v) <- excelMap) {
    val size = v.size

    //创建第一个sheet（页），命名为 new sheet
    val sheet = wb.createSheet(k)
    // 设置excel每列宽度
    sheet.setColumnWidth(0, 4000)
    sheet.setColumnWidth(1, 3500)
    sheet.setColumnWidth(2, 3500)
    sheet.setColumnWidth(3, 3500)
    sheet.setColumnWidth(4, 4000)
    sheet.setColumnWidth(5, 3500)
    sheet.setColumnWidth(6, 3500)
    sheet.setColumnWidth(7, 3500)
    sheet.setColumnWidth(8, 9500)
    sheet.setColumnWidth(9, 9500)


    //Row 行
    //Cell 方格
    // Row 和 Cell 都是从0开始计数的

    // 创建一行，在页sheet上
    val row = sheet.createRow(0)
    // 在row行上创建一个方格
    //val cell = row.createCell("");
    //设置方格的显示
    //cell.setCellValue(2);
    //cell.setCellStyle(style)

    // Or do it on one line.
    row.createCell(0).setCellValue("问句")
    row.createCell(1).setCellValue("期望业务")
    row.createCell(2).setCellValue("线上环境业务")
    row.createCell(3).setCellValue("测试环境业务")
    row.createCell(4).setCellValue("线上是否有数据")
    row.createCell(5).setCellValue("测试是否有数据")
    row.createCell(6).setCellValue("线上语义")
    row.createCell(7).setCellValue("测试语义")
    row.createCell(8).setCellValue("线上URL")
    row.createCell(9).setCellValue("测试URL")

    for (i <- 0 until size) {
      val flag = if (v(i)._2.equals(v(i)._3) && v(i)._2.equals(v(i)._4) && v(i)._3.equals(v(i)._4)) false else true
      val row = sheet.createRow(i + 1)
      val cell0 = row.createCell(0)
      cell0.setCellValue(v(i)._1)
      val cell1 = row.createCell(1)
      cell1.setCellValue(v(i)._2)
      val cell2 = row.createCell(2)
      cell2.setCellValue(v(i)._3)
      val cell3 = row.createCell(3)
      cell3.setCellValue(v(i)._4)
      val cell4 = row.createCell(4)
      cell4.setCellValue(v(i)._5)
      val cell5 = row.createCell(5)
      cell5.setCellValue(v(i)._6)
      val cell6 = row.createCell(6)
      cell6.setCellValue(v(i)._7)
      val cell7 = row.createCell(7)
      cell7.setCellValue(v(i)._8)
      val cell8 = row.createCell(8)
      cell8.setCellValue(v(i)._9)
      val cell9 = row.createCell(9)
      cell9.setCellValue(v(i)._10)

      if (flag) {
        cell1.setCellStyle(style)
        cell2.setCellStyle(style)
        cell3.setCellStyle(style)
      }
    }
  }

  //创建一个文件 命名为workbook.xls
  val fileOut = new FileOutputStream("业务测试结果-new.xls")
  // 把上面创建的工作簿输出到文件中
  wb.write(fileOut)
  //关闭输出流
  fileOut.close()
}
