package com.fowlart.main
import com.fowlart.main.state.ScalaBotVisitor
import com.fowlart.main.state.Order

object OrderHandler {

  def handleOrder(scalaBotVisitor: ScalaBotVisitor): Order = {
    val name = if (scalaBotVisitor.name!=null) scalaBotVisitor.name else scalaBotVisitor.user.getFirstName
    val userId  = scalaBotVisitor.userId
    val phoneNumber = scalaBotVisitor.phoneNumber
    val orderId = s"$userId-${System.nanoTime()}"
    val orderDate = java.time.LocalDate.now.toString

    Order(orderId,orderDate,userId,name,phoneNumber,scalaBotVisitor.bucket,false)
  }
}
