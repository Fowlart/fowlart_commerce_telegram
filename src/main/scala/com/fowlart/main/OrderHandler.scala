package com.fowlart.main
import com.fowlart.main.state.ScalaBotVisitor
import com.fowlart.main.state.Order

import java.io.File
import java.nio.file.{Files, OpenOption, Paths}
import java.nio.charset.{CharsetEncoder, StandardCharsets}
import scala.collection.JavaConverters._

object OrderHandler {

  def handleOrder(scalaBotVisitor: ScalaBotVisitor): Order = {
    val name = if (scalaBotVisitor.name!=null) scalaBotVisitor.name else scalaBotVisitor.user.getFirstName
    val userId  = scalaBotVisitor.userId
    val phoneNumber = scalaBotVisitor.phoneNumber
    val orderId = s"$userId-${System.nanoTime()}"
    val orderDate = java.time.LocalDate.now.toString

    Order(orderId,orderDate,userId,name,phoneNumber,scalaBotVisitor.bucket,false)
  }

  def saveOrderAsCsv(order: Order, path: String): File = {

    val header = s"userId,userName,userPhoneNumber,date,itemId,itemGroup,itemName,qty"

    val lines: List[String] = header +: order.orderBucket.toList
      .map(item => s"${order.userId},${order.userName},${order.userPhoneNumber},${order.date},${item.id()},${item.group()},${item.name()},${if (item.qty() == null) 0 else item.qty()}")
      .map(str => new String(str.getBytes(), "UTF-8"))

    val createdPath = Files.write(Paths.get(path), lines.asJava)

    createdPath.toFile
  }
}
