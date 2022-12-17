package com.fowlart.main
import com.fowlart.main.in_mem_catalog.{Catalog, Item}
import com.fowlart.main.messages._
import com.fowlart.main.state.ScalaBotVisitor
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

import scala.collection.JavaConverters._

object BotMessageHandler {

  private val scalaHelper: ScalaHelper = new ScalaHelper

  def handleMessageOrCommand(
                              scalaBotVisitor: ScalaBotVisitor,
                              msg: String,
                              keyboardHelper: KeyboardHelper,
                              chatId: Long,
                              catalog: Catalog): HandlerResponse = {

    val tuple = (scalaBotVisitor,msg)

    tuple match {
      // user in edit-name mode
      case (ScalaBotVisitor(name,true,_, false, _, user, _, _), message: String) => {
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getFullNameReceivedText(name)).replyMarkup(keyboardHelper.buildMainMenuReply).build
        val updatedBotVisitor = scalaBotVisitor.copy(name = msg, isNameEditingMode = false)
        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
      }

      // user in edit-phone-number mode, phone number is valid
      case (ScalaBotVisitor(name,_,_, true, _, user, _, _), message: String) if scalaHelper.isPhoneNumber(message) => {
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getPhoneNumberReceivedText(name)).replyMarkup(keyboardHelper.buildMainMenuReply).build
        val updatedBotVisitor = scalaBotVisitor.copy(phoneNumber = msg, isPhoneNumberFillingMode = false)
        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
      }

      // user in edit phone number mode, phone number is not valid
      case (ScalaBotVisitor(name,_,_, true, _, user, _, _), _) => {
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getPhoneEditingText(scalaBotVisitor.user.getId)).replyMarkup(keyboardHelper.buildInPhoneEditingModeMenu).build
        val updatedBotVisitor = scalaBotVisitor.copy(phoneNumber = "[номер не вказаний/вказаний не вірно]", isPhoneNumberFillingMode = true)
        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
      }

      // user in quantity-edit mode at the basket and entered text is numeric
      case (ScalaBotVisitor(name,isNameEditingMode,phoneNumber, false, itemToEditQty, user, userId, bucket), textFromUser: String) if itemToEditQty != null && scalaHelper.isNumeric(textFromUser) => {
        val qty = textFromUser.toInt
        val toAdd = new Item(itemToEditQty.id, itemToEditQty.name, itemToEditQty.price, itemToEditQty.group, qty)
        val updateScalaBotVisitor = state.ScalaBotVisitor(name,isNameEditingMode,phoneNumber, false, null, user, userId, bucket - itemToEditQty + toAdd)
        val message = SendMessage.builder.chatId(chatId).text("Кількість прийнята. Корзину збережено. Не забудьте відправити замовлення.").replyMarkup(keyboardHelper.buildMainMenuReply).build
        ResponseWithSendMessageAndScalaBotVisitor(message, updateScalaBotVisitor)
      }

      // user in quantity-edit mode at the basket and entered text is NOT numeric
      case (ScalaBotVisitor(name,isNameEditingMode,phoneNumber, false, itemToEditQty, user, userId, bucket), textFromUser: String) if itemToEditQty != null => {
        val visitor = state.ScalaBotVisitor(name,isNameEditingMode,phoneNumber, false, itemToEditQty, user, userId, bucket)
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getItemQtyWrongEnteredNumber(visitor)).build
        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, visitor)
      }

      // user trying to add item from catalog to the basket
      case (ScalaBotVisitor(name,isNameEditingMode,phoneNumber,false, null, user, userId, bucket), textFromUser: String) if textFromUser.startsWith(GOOD_ADD_COMMAND) => {
        val itemId = textFromUser.replaceAll("/", "")
        val matchedItem = catalog.getItemList.asScala.find((it: Item) => it.id.equalsIgnoreCase(itemId))
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getItemAcceptedText(matchedItem.get)).replyMarkup(keyboardHelper.buildMainMenuReply).build
        if (matchedItem.isDefined) {
          val updatedScalaVisitor = state.ScalaBotVisitor(name,isNameEditingMode,phoneNumber, false, null, user, userId, bucket + matchedItem.get)
          ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedScalaVisitor)
        } else {
          sendMessage.setText(scalaHelper.getItemNotAcceptedText())
          ResponseWithSendMessageAndScalaBotVisitor(sendMessage, state.ScalaBotVisitor(name,isNameEditingMode,phoneNumber, false, null, user, userId, bucket))
        }
      }

      // default
      case (visitor, _) => {
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getMainMenuText(visitor.name)).replyMarkup(keyboardHelper.buildMainMenuReply).build
        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, visitor)
      }
    }
  }
}
