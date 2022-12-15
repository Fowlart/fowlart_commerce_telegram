package com.fowlart.main

import com.fowlart.main.state.{BotVisitor, ScalaBotVisitor}

import scala.collection.JavaConverters._

object BotVisitorToScalaBotVisitorConverter {

  def convertBotVisitorToScalaBotVisitor(botVisitor: BotVisitor): ScalaBotVisitor = {
    state.ScalaBotVisitor(
      botVisitor.getName,
      botVisitor.isNameEditingMode,
      botVisitor.getPhoneNumber,
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
    botVisitor.setName(scalaBotVisitor.name)
    botVisitor.setNameEditingMode(scalaBotVisitor.isNameEditingMode)
    botVisitor
  }

}
