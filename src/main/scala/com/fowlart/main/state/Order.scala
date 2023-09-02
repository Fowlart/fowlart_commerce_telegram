package com.fowlart.main.state

import com.fowlart.main.state.cosmos.Item

case class Order (orderId:String,
                  date: String,
                  userId: String,
                  userName: String,
                  userPhoneNumber: String,
                  orderBucket: Set[Item],
                  isDelivered: Boolean) {

  override def toString: String = {
    val bucketString = if (orderBucket.nonEmpty) s"${orderBucket.map(i => s"[${i.name} - ${i.qty}] ").reduce((s1, s2) => s"$s1$s2")}" else s"[]"
    s"""
       |orderId: $orderId
       |date: $date
       |userId: $userId
       |userName: $userName
       |userPhoneNumber: $userPhoneNumber
       |userId: $userId
       |orderBucket: $bucketString
       |""".stripMargin
  }
}
