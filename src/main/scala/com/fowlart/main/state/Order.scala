package com.fowlart.main.state

import com.fowlart.main.in_mem_catalog.Item

case class Order (orderId:String,
                  date: String,
                  userId: String,
                  userName: String,
                  userPhoneNumber: String,
                  orderBucket: Set[Item],
                  isDelivered: Boolean)
