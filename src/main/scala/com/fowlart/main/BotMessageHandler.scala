package com.fowlart.main
import com.fowlart.main.in_mem_catalog.{Catalog, Item}
import com.fowlart.main.messages._
import com.fowlart.main.state.ScalaBotVisitor
import com.google.gson.Gson
import org.slf4j.{Logger, LoggerFactory}
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.User

import java.util.Date
import scala.collection.JavaConverters._

object BotMessageHandler {

  private val scalaHelper: ScalaHelper = new ScalaHelper

  private val logger = LoggerFactory.getLogger(classOf[Bot])

  def handleMessageOrCommand(
                              scalaBotVisitor: ScalaBotVisitor,
                              msg: String,
                              keyboardHelper: KeyboardHelper,
                              chatId: Long,
                              catalog: Catalog,
                              itemNotFoundImgPath: String,
                              inputForImgPath: String): HandlerResponse = {

    val tuple = (scalaBotVisitor,msg)

    logger.info(s"Entered BotMessageHandler. UserId $chatId, threadId ${Thread.currentThread.getId}")

    tuple match {

      case (ScalaBotVisitor(_,true,_, false, _, _, _, _), message: String) =>
        handleUserInNameEditMode(scalaBotVisitor, message, keyboardHelper, chatId)

      case (ScalaBotVisitor(_,_,_, true, _, _, _, _), message: String) if scalaHelper.isPhoneNumber(message) =>
        handleUserEnteredCorrectPhoneNumber(scalaBotVisitor, message, keyboardHelper, chatId)

      case (ScalaBotVisitor(_,_,_, true, _, _, _, _), message)  if !scalaHelper.isPhoneNumber(message) =>
        handleUserEnteredIncorrectPhoneNumber(scalaBotVisitor, keyboardHelper, chatId)

      case (ScalaBotVisitor(name,isNameEditingMode,phoneNumber, false, itemToEditQty, user, userId, bucket), textFromUser: String) if itemToEditQty != null && scalaHelper.isNumeric(textFromUser) =>
        handleItemQuantityEditWithNumericValue(keyboardHelper, chatId, name, isNameEditingMode, phoneNumber, itemToEditQty, user, userId, bucket, textFromUser)

      case (ScalaBotVisitor(name,isNameEditingMode,phoneNumber, false, itemToEditQty, user, userId, bucket),_) if itemToEditQty != null =>
        handleItemQuantityEditWithNotNumericValue(chatId, name, isNameEditingMode, phoneNumber, itemToEditQty, user, userId, bucket)

      case (ScalaBotVisitor(name,isNameEditingMode,phoneNumber,false, null, user, userId, bucket), textFromUser: String) if textFromUser.startsWith(GOOD_ADD_COMMAND) =>
        handleUserAddedItemToBasket(chatId, catalog, name, isNameEditingMode, phoneNumber, user, userId, bucket, textFromUser,itemNotFoundImgPath,inputForImgPath,keyboardHelper)

      // default
      case (visitor, _) =>
        logger.info(s"some not recognized message from visitor: ${visitor.userId}")
        val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getMainMenuText(visitor)).replyMarkup(keyboardHelper.buildMainMenuReply).build
        ResponseWithSendMessageAndScalaBotVisitor(sendMessage, visitor)
    }
  }

  private def handleUserAddedItemToBasket(chatId: Long,
                                          catalog: Catalog,
                                          name: String,
                                          isNameEditingMode: Boolean,
                                          phoneNumber: String,
                                          user: User,
                                          userId: String,
                                          bucket: Set[Item],
                                          textFromUser: String,
                                          itemNotFoundImgPath: String,
                                          inputForImgPath: String,
                                          keyboardHelper: KeyboardHelper) = {

    logger.info(s"handleUserAddedItemToBasket method, userID: $userId}")
    val itemId = textFromUser.replaceAll("/", "")
    val matchedItem = catalog.getItemList.asScala.find((it: Item) => it.id.equalsIgnoreCase(itemId))

    val sendMessage = SendMessage.builder.chatId(chatId)
      .text(scalaHelper.getEditItemQtyMsg(matchedItem.get))
      .parseMode("html")
      .build

    if (matchedItem.isDefined) {
      val updatedScalaVisitor = state.ScalaBotVisitor(name, isNameEditingMode, phoneNumber, false, matchedItem.get, user, userId, bucket + matchedItem.get)

      val photoMessage = scalaHelper.getItemMessageWithPhotoWithDeleteButtonOnly(
          chatId,
          matchedItem.get,
          itemNotFoundImgPath,
          inputForImgPath,
          keyboardHelper)

      ResponseWithPhotoMessageAndScalaBotVisitor(photoMessage, updatedScalaVisitor)
    } else {
      sendMessage.setText(scalaHelper.getItemNotAcceptedText())
      ResponseWithSendMessageAndScalaBotVisitor(sendMessage, state.ScalaBotVisitor(name, isNameEditingMode, phoneNumber, false, null, user, userId, bucket))
    }
  }
  private def handleItemQuantityEditWithNotNumericValue(chatId: Long, name: String, isNameEditingMode: Boolean, phoneNumber: String, itemToEditQty: Item, user: User, userId: String, bucket: Set[Item]) = {
    logger.info(s"handleItemQuantityEditWithNotNumericValue method, userID: $userId")
    val visitor = state.ScalaBotVisitor(name, isNameEditingMode, phoneNumber, false, itemToEditQty, user, userId, bucket)
    val sendMessage = SendMessage.builder.chatId(chatId).parseMode("html").text(scalaHelper.getItemQtyWrongEnteredNumber(visitor)).build
    ResponseWithSendMessageAndScalaBotVisitor(sendMessage, visitor)
  }
  private def handleItemQuantityEditWithNumericValue(keyboardHelper: KeyboardHelper, chatId: Long, name: String, isNameEditingMode: Boolean, phoneNumber: String, itemToEditQty: Item, user: User, userId: String, bucket: Set[Item], textFromUser: String) = {
    logger.info(s"handleItemQuantityEditWithNumericValue method, userID: $userId")
    val qty = textFromUser.toInt
    val toAdd = new Item(itemToEditQty.id, itemToEditQty.name, itemToEditQty.price, itemToEditQty.group, qty)
    val updateScalaBotVisitor = state.ScalaBotVisitor(name, isNameEditingMode, phoneNumber, false, null, user, userId, bucket - itemToEditQty + toAdd)
    val message = SendMessage.builder.chatId(chatId).text("Кількість прийнята. Корзину збережено. Не забудьте відправити замовлення.").replyMarkup(keyboardHelper.buildMainMenuReply).build
    ResponseWithSendMessageAndScalaBotVisitor(message, updateScalaBotVisitor)
  }

  private def handleUserEnteredIncorrectPhoneNumber(scalaBotVisitor: ScalaBotVisitor, keyboardHelper: KeyboardHelper, chatId: Long) = {
    logger.info(s"handleUserEnteredIncorrectPhoneNumber method, userID: ${scalaBotVisitor.userId}")
    val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getPhoneEditingText(scalaBotVisitor.user.getId)).replyMarkup(keyboardHelper.buildInPhoneEditingModeMenu).build
    val updatedBotVisitor = scalaBotVisitor.copy(phoneNumber = "[номер не вказаний/вказаний не вірно]", isPhoneNumberFillingMode = true)
    ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
  }

  private def handleUserEnteredCorrectPhoneNumber(scalaBotVisitor: ScalaBotVisitor, msg: String, keyboardHelper: KeyboardHelper, chatId: Long) = {
    logger.info(s"handleUserEnteredCorrectPhoneNumber method, userID: ${scalaBotVisitor.userId}")
    val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getPhoneNumberReceivedText()).replyMarkup(keyboardHelper.buildMainMenuReply).build
    val updatedBotVisitor = scalaBotVisitor.copy(phoneNumber = msg, isPhoneNumberFillingMode = false)
    ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
  }

  private def handleUserInNameEditMode(scalaBotVisitor: ScalaBotVisitor, msg: String, keyboardHelper: KeyboardHelper, chatId: Long) = {
    logger.info(s"handleUserInNameEditMode method, userID: ${scalaBotVisitor.userId}")
    val sendMessage = SendMessage.builder.chatId(chatId).text(scalaHelper.getFullNameReceivedText()).replyMarkup(keyboardHelper.buildMainMenuReply).build
    val updatedBotVisitor = scalaBotVisitor.copy(name = msg, isNameEditingMode = false)
    ResponseWithSendMessageAndScalaBotVisitor(sendMessage, updatedBotVisitor)
  }
}
