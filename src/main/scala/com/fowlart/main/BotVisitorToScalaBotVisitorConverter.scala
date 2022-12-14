package com.fowlart.main

import com.fowlart.main.state.BotVisitor
import scala.collection.JavaConverters._
import com.fowlart.main.ScalaBotVisitor

object BotVisitorToScalaBotVisitorConverter {

  def convertBotVisitorToScalaBotVisitor(botVisitor: BotVisitor): ScalaBotVisitor = {
    ScalaBotVisitor(botVisitor.getPhoneNumber,
      botVisitor.isPhoneNumberFillingMode,
      botVisitor.getItemToEditQty,
      botVisitor.getUser,
      botVisitor.getUserId,
      botVisitor.getBucket.asScala.toSet)
  }
  
  def convertToJavaBotVisitor(scalaBotVisitor: ScalaBotVisitor): BotVisitor = {
    val botVisitor = new BotVisitor(scalaBotVisitor.user,scalaBotVisitor.user.getId)
    botVisitor.setBucket(scalaBotVisitor.bucket.asJava)
    botVisitor.setPhoneNumber(scalaBotVisitor.phoneNumber)
    botVisitor.setItemToEditQty(scalaBotVisitor.itemToEditQty)
    botVisitor.setPhoneNumberFillingMode(scalaBotVisitor.isPhoneNumberFillingMode)
    botVisitor
  }

}
