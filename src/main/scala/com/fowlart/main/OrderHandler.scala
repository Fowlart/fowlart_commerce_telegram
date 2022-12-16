package com.fowlart.main
import com.fowlart.main.state.ScalaBotVisitor
import com.fowlart.main.state.Order

import java.io.File
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets

object OrderHandler {

  def handleOrder(scalaBotVisitor: ScalaBotVisitor): Order = {
    val name = if (scalaBotVisitor.name!=null) scalaBotVisitor.name else scalaBotVisitor.user.getFirstName
    val userId  = scalaBotVisitor.userId
    val phoneNumber = scalaBotVisitor.phoneNumber
    val orderId = s"$userId-${System.nanoTime()}"
    val orderDate = java.time.LocalDate.now.toString

    Order(orderId,orderDate,userId,name,phoneNumber,scalaBotVisitor.bucket,false)
  }

  def saveOrderAsCsv(order: Order,path: String): Boolean = {

    val header = s"userId,userName,userPhoneNumber,date,itemId,itemGroup,itemName,qty \n"

    val lines = order.orderBucket
      .map(item=>s"${order.userId},${order.userName},${order.userPhoneNumber},${order.date},${item.id()},${item.group()},${item.name()},${if (item.qty()==null) 0 else item.qty()}")
      .reduce((l1,l2)=>s"$l1\n$l2")

    // todo: smart exception handling
    Files.write(Paths.get(path), s"$header$lines".getBytes(StandardCharsets.UTF_8))
    true
  }
}
