package com.fowlart.main.state


import com.fowlart.main.state.cosmos.Item
import org.telegram.telegrambots.meta.api.objects.User

case class ScalaBotVisitor(
                            name: String,
                            isNameEditingMode: Boolean,
                        phoneNumber: String, 
                        isPhoneNumberFillingMode: Boolean,
                        itemToEditQty: Item,
                        user: User, 
                        userId: String, 
                        bucket: Set[Item]) {

  override def toString: String = {
    val bucketString = if (bucket.nonEmpty) s"${bucket.map(i=>s"[${i.name} - ${i.qty}] ").reduce((s1, s2)=>s"$s1$s2")}" else s"[]"
    val itemToEditString = if (itemToEditQty!=null) itemToEditQty.name else "[]"
    s"""
      |name: $name
      |isNameEditingMode: $isNameEditingMode
      |phoneNumber: $phoneNumber
      |isPhoneNumberFillingMode: $isPhoneNumberFillingMode
      |itemToEditQty: $itemToEditString
      |userId: $userId
      |bucket: $bucketString
      |""".stripMargin
  }

}
