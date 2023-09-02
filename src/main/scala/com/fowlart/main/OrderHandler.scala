package com.fowlart.main
import com.fowlart.main.state.ScalaBotVisitor
import com.fowlart.main.state.cosmos.Order

import java.io.File
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

object OrderHandler {

  def handleOrder(scalaBotVisitor: ScalaBotVisitor): Order = {
    val name =
      if (scalaBotVisitor.name != null) scalaBotVisitor.name
      else scalaBotVisitor.user.getFirstName
    val userId = scalaBotVisitor.userId
    val phoneNumber = scalaBotVisitor.phoneNumber
    val orderId = s"$userId-${System.nanoTime()}"
    val orderDate = java.time.LocalDate.now.toString

    val order = new Order()
    order.setOrderId(orderId)
    order.setDate(orderDate)
    order.setUserId(userId)
    order.setUserName(name)
    order.setUserPhoneNumber(phoneNumber)
    order.setOrderBucket(scalaBotVisitor.bucket.asJava)

    order
  }

  def saveOrderAsCsv(order: Order, path: String): File = {

    val header =
      s"userId;userName;userPhoneNumber;date;itemId;itemGroup;itemName;qty"

    val lines: List[String] = header +: order.getOrderBucket.toList
      .map(item =>
        s"${order.getUserId};${order.getUserName};${order.getUserPhoneNumber};${order.getDate};${item
          .id()};${item.group()};${item.name()};${if (item.qty() == null) 0
        else item.qty()}"
      )
      .map(str => new String(str.getBytes(), "UTF-8"))

    val createdPath = Files.write(Paths.get(path), lines.asJava)

    createdPath.toFile
  }
}
