package com.fowlart.main

import com.fowlart.main.in_mem_catalog.Item
import com.fowlart.main.state.BotVisitor
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import scala.collection.JavaConverters._
import java.util.regex.Pattern

object MessageProcessor {

  private def isNumeric(strNum: String): Boolean = {
    val pattern = Pattern.compile("-?\\d+")
    if (strNum == null) return false
    pattern.matcher(strNum).matches
  }

  private def getBucketMessage(visitor: BotVisitor,
                               userId: String,
                               keyboardHelper: KeyboardHelper) = {

    val itemList = visitor.getBucket.asScala.filter(it => it != null).map(item => s" ⏺ $item").toList
    val textInBucket = if (itemList.isEmpty) "[Корзина порожня]" else itemList.reduce((i1,i2)=>s"$i1\n\n$i2")

    SendMessage.builder.chatId(userId)
      .text(
        s"""
           |Корзина:
           |
           |$textInBucket
           |""")
      .replyMarkup(keyboardHelper.buildBucketKeyboardMenu).build
  }

  def processMessage(msg: Msg): OutputBotMessage = {
    msg match {

      case InputBotUpdate(update,service,keyboardHelper,catalog) => {

        val textFromUser: Any = update.getMessage.getText
        val userId = update.getMessage.getFrom.getId.toString
        val chatId = update.getMessage.getChatId
        val userFirstName = update.getMessage.getFrom.getFirstName
        val scalaTextHelper = new ScalaHelper
        val botVisitor = service.getBotVisitorByUserId(userId)

        val standardAnswer = SendMessage
          .builder
          .chatId(chatId.toString)
          .text(scalaTextHelper.getMainMenuText(userFirstName))
          .replyMarkup(keyboardHelper.buildMainMenuReply).build


        textFromUser match {
          case msg: String if msg.startsWith(REMOVE_COMMAND) => {
            println("Removing item case")
            val toRemove = botVisitor.getItemToEditQty
            botVisitor.getBucket.remove(toRemove)
            botVisitor.setItemToEditQty(null)
            service.saveBotVisitor(botVisitor)
            OutputBotMessage(botVisitor = botVisitor, getBucketMessage(botVisitor, userId, keyboardHelper))
          }
          case msg: String if msg.startsWith(GOOD_ADD_COMMAND) => {
            val textFromUserCleaned = msg.replaceAll("/", "")
            val maybeItem = catalog.getItemList.stream.filter((item: Item) => item.id.equalsIgnoreCase(textFromUserCleaned)).findFirst
            if (maybeItem.isPresent) {
              val item = maybeItem.get
              botVisitor.getBucket.add(item)
              service.saveBotVisitor(botVisitor)
              standardAnswer.setText(scalaTextHelper.getItemAcceptedText(item))
            } else {
              standardAnswer.setText(scalaTextHelper.getItemNotAcceptedText)
              service.saveBotVisitor(botVisitor)
            }
            OutputBotMessage(botVisitor, standardAnswer)
          }
          case msg: String if isNumeric(msg) => {
            val toRemove = botVisitor.getItemToEditQty
            val qty = msg.toInt
            val toAdd = new Item(toRemove.id, toRemove.name, toRemove.price, toRemove.group, qty)
            botVisitor.setItemToEditQty(null)
            botVisitor.getBucket.remove(toRemove)
            botVisitor.getBucket.add(toAdd)
            service.saveBotVisitor(botVisitor)
            OutputBotMessage(botVisitor = botVisitor, getBucketMessage(botVisitor, userId, keyboardHelper))
          }
          case _ => {
            println("Unpredictable result")
            OutputBotMessage(botVisitor, standardAnswer)
          }
        }

      }
    }
  }
}
