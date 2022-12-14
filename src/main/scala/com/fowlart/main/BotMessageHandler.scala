package com.fowlart.main
import com.fowlart.main.in_mem_catalog.{Catalog, Item}
import com.fowlart.main.messages._
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

import scala.collection.JavaConverters._

object BotMessageHandler {

  private val REMOVE_COMMAND = "/remove"
  private val scalaHelper: ScalaHelper = new ScalaHelper
  private val GOOD_ADD_COMMAND = "/ID"

  def handleMessageOrCommand(
                              scalaBotVisitor: ScalaBotVisitor,
                              msg: String,
                              keyboardHelper: KeyboardHelper,
                              chatId: Long,
                              catalog: Catalog): HandlerResponse = {

    val tuple = (scalaBotVisitor,msg)

    tuple match {
      /** user in edit phone number mode, phone number is valid */
      case (ScalaBotVisitor(_, true, _, user, _, _), m: String) if scalaHelper.isPhoneNumber(m) => {
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getMainMenuText(user.getUserName)).replyMarkup(keyboardHelper.buildMainMenuReply).build

        val updatedBotVisitor = scalaBotVisitor.copy(phoneNumber = msg, isPhoneNumberFillingMode = false)

        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
      }

      /** user in edit phone number mode, phone number is not valid */
      case (ScalaBotVisitor(_, true, _, user, _, _), _) => {
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getPhoneEditingText(scalaBotVisitor.user.getId)).replyMarkup(keyboardHelper.buildInPhoneEditingModeMenu).build

        val updatedBotVisitor = scalaBotVisitor.copy(phoneNumber = msg, isPhoneNumberFillingMode = false)

        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
      }

      /** a user trying to remove an item from the basket */
      case (ScalaBotVisitor(phoneNumber, false, itemToEditQty, user, userId, bucket), m: String) if m.startsWith(REMOVE_COMMAND) => {
        val newBucket = bucket - itemToEditQty
        val updateScalaBotVisitor = ScalaBotVisitor(phoneNumber, false, null, user, userId, newBucket)
        val message = scalaHelper.getBucketMessageForScalaBotVisitor(updateScalaBotVisitor, updateScalaBotVisitor.userId, keyboardHelper)
        ResponseWithSendMessageAndScalaBotVisitor(message, updateScalaBotVisitor)
      }

      /** user in quantity-edit mode at the basket and entered text is numeric */
      case (ScalaBotVisitor(phoneNumber, false, itemToEditQty, user, userId, bucket), textFromUser: String) if itemToEditQty != null && scalaHelper.isNumeric(textFromUser) => {
        val qty = textFromUser.toInt
        val toAdd = new Item(itemToEditQty.id, itemToEditQty.name, itemToEditQty.price, itemToEditQty.group, qty)
        val updateScalaBotVisitor = ScalaBotVisitor(phoneNumber, false, null, user, userId, bucket - itemToEditQty + toAdd)
        val message = scalaHelper.getBucketMessageForScalaBotVisitor(updateScalaBotVisitor, updateScalaBotVisitor.userId, keyboardHelper)
        ResponseWithSendMessageAndScalaBotVisitor(message, updateScalaBotVisitor)
      }

      /** user in quantity-edit mode at the basket and entered text is NOT numeric */
      case (ScalaBotVisitor(phoneNumber, false, itemToEditQty, user, userId, bucket), textFromUser: String) if itemToEditQty != null => {
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getItemQtyWrongEnteredNumber(user.getFirstName)).replyMarkup(keyboardHelper.buildMainMenuReply).build

        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, ScalaBotVisitor(phoneNumber, false, itemToEditQty, user, userId, bucket))
      }

      /** user trying to add item from catalog to the basket */
      case (ScalaBotVisitor(phoneNumber, false, null, user, userId, bucket), textFromUser: String) if textFromUser.startsWith(GOOD_ADD_COMMAND) => {
        val itemId = textFromUser.replaceAll("/", "")
        val matchedItem = catalog.getItemList.asScala.find((it: Item) => it.id.equalsIgnoreCase(itemId))

        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getItemAcceptedText(matchedItem.get)).replyMarkup(keyboardHelper.buildMainMenuReply).build

        if (matchedItem.isDefined) {
          val updatedScalaVisitor = ScalaBotVisitor(phoneNumber, false, null, user, userId, bucket + matchedItem.get)
          ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedScalaVisitor)
        } else {
          sendMessage.setText(scalaHelper.getItemNotAcceptedText())
          ResponseWithSendMessageAndScalaBotVisitor(sendMessage, ScalaBotVisitor(phoneNumber, false, null, user, userId, bucket))
        }
      }

      // DEFAULT
      case (visitor, _) => {
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getMainMenuText(visitor.user.getFirstName)).replyMarkup(keyboardHelper.buildMainMenuReply).build
        val updatedBotVisitor = scalaBotVisitor.copy(phoneNumber = msg, isPhoneNumberFillingMode = false)
        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
      }
    }
  }
}
