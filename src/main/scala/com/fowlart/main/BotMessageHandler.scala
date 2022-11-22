package com.fowlart.main
import com.fowlart.main.in_mem_catalog.Item
import com.fowlart.main.messages._

import scala.collection.JavaConverters._

object BotMessageHandler {

  val scalaHelper: ScalaHelper = new ScalaHelper

  def handleBotMessage(botMessage: BotMessage): HandlerResponse = {

    botMessage match {

      case RemoveItemFromBucketMessage(visitor,botVisitorService,keyboardHelper) => {
        val toRemove = visitor.getItemToEditQty
        visitor.getBucket.remove(toRemove)
        visitor.setItemToEditQty(null)
        botVisitorService.saveBotVisitor(visitor)
        val message = scalaHelper.getBucketMessage(visitor, visitor.getUserId, keyboardHelper)
        ResponseWithSendMessage(message)
      }


      case EditQtyForItemMessage(qty, botVisitor, botVisitorService,keyboardHelper) => {
        val toRemove = botVisitor.getItemToEditQty
        val toAdd = new Item(toRemove.id, toRemove.name, toRemove.price, toRemove.group, qty)
        botVisitor.setItemToEditQty(null)
        botVisitor.getBucket.remove(toRemove)
        botVisitor.getBucket.add(toAdd)
        botVisitorService.saveBotVisitor(botVisitor)
        val sendMessage = scalaHelper.getBucketMessage(botVisitor, botVisitor.getUserId, keyboardHelper)
        ResponseWithSendMessage(sendMessage)
      }


      case ItemAddToBucketMessage(item,visitor,botVisitorService, keyboardHelper,catalog,sendMessage) => {
        val matchedItem = catalog.getItemList.asScala.find((it: Item) => it.id.equalsIgnoreCase(item))
        if (matchedItem.isDefined) {
          visitor.getBucket.add(matchedItem.get)
          botVisitorService.saveBotVisitor(visitor)
          sendMessage.setText(scalaHelper.getItemAcceptedText(matchedItem.get))
          sendMessage.setReplyMarkup(keyboardHelper.buildMainMenuReply())
          ResponseWithSendMessage(sendMessage)
        }
        else {
          sendMessage.setText(scalaHelper.getItemNotAcceptedText())
          ResponseWithSendMessage(sendMessage)
        }
      }
    }
  }
}
