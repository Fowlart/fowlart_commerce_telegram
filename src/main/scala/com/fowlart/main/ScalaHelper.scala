package com.fowlart.main

import com.fowlart.main.in_mem_catalog.Item
import com.fowlart.main.state.{BotVisitor, Order, ScalaBotVisitor}
import org.telegram.telegrambots.meta.api.methods.send.{SendMessage, SendPhoto}
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

import java.io.{File, IOException}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Path}
import java.util
import java.util.function.BiPredicate
import java.util.{List, Optional}
import java.util.regex.Pattern
import java.util.stream.{Collectors, Stream}
import javax.activation.MimetypesFileTypeMap
import scala.collection.JavaConverters._

class ScalaHelper {


   def getItemMessageWithPhoto(chatId: Long,
                               item: Item,
                               itemNotFoundImgPath: String,
                               inputForImgPath: String,
                               keyboardHelper: KeyboardHelper) = {

     val biPredicate: BiPredicate[Path,BasicFileAttributes] = (path, _) => {
       val theFile = path.toFile
       val mimetype = new MimetypesFileTypeMap().getContentType(theFile)
       val theType = mimetype.split("/")(0)
       path.getFileName.toString.toLowerCase.contains(item.name.toLowerCase) && theType == "image"
     }

     val itemImgOp = Files.find(Path.of(s"$inputForImgPath/"), 1, biPredicate).findFirst()

     val noImageImg = new File(itemNotFoundImgPath)

     val response = SendPhoto
       .builder
       .caption(getEditItemQtyMsg(item))
       .replyMarkup(keyboardHelper.buildBucketItemKeyboardMenu(item.id))
       .chatId(chatId)
       .parseMode("html")
       .photo(new InputFile(noImageImg)).build

     if (itemImgOp.isPresent && itemImgOp.get.toFile.exists) {
       response.setPhoto(new InputFile(itemImgOp.get.toFile))
     }

     response
   }

  def getItemMessageWithPhotoWithDeleteButtonOnly(chatId: Long,
                                                  item: Item,
                                                  itemNotFoundImgPath: String,
                                                  inputForImgPath: String,
                                                  keyboardHelper: KeyboardHelper) = {

    val response = getItemMessageWithPhoto(chatId,item,itemNotFoundImgPath,inputForImgPath,keyboardHelper)
    response.setReplyMarkup(keyboardHelper.buildAddToBucketItemKeyboardMenu(item.id()))
    response
  }

  def getItemMessageWithPhotoInBucket(chatId: Long, item: Item, itemNotFoundImgPath: String, inputForImgPath: String, keyboardHelper: KeyboardHelper) = {

    val response = getItemMessageWithPhoto(chatId, item, itemNotFoundImgPath, inputForImgPath, keyboardHelper)
    response.setCaption(getItemBucketMessage(item))
    response
  }


  def buildSimpleReplyMessage(userId: Long, text: String, markUp: InlineKeyboardMarkup): SendMessage = {

    SendMessage
      .builder
      .chatId(userId)
      .text(text)
      .replyMarkup(markUp)
      .build
  }

  def getItemBucketIntroMessage(userId: String,keyboardHelper: KeyboardHelper): SendMessage = {
    SendMessage.builder.chatId(userId)
      .replyMarkup(keyboardHelper.buildBucketKeyboardMenu())
      .text(
        s"""
           |????
           | ??????????, ???????????? ?????????????? ?? ??????????????.
           | ???????? ??????????, ???????????????????????? ???? ?????????????? ?????????????????? ??????????????.
           |""".stripMargin).build
  }

  def getItemBucketMessage(item: Item): String =
    s"""
       |${item.toString2}
       |""".stripMargin

  def getEditItemQtyMsg(item: Item): String =
    s"""
       |?????????????? ?????????????????? ???????????? <b>??????????</b> 0??????1??????2??????3?????? ???????????????????? ????????????:
       |
       |${item.toString3}
       |""".stripMargin

  def getEmptyBucketMessage(keyboardHelper: KeyboardHelper, userId: Long): SendMessage = {

    SendMessage.builder.chatId(userId)
      .text( "\uD83D\uDDD1[?????????????? ??????????????]\uD83D\uDDD1")
      .replyMarkup(keyboardHelper.buildMainMenuReply()).build
  }

  def isNumeric(strNum: String): Boolean = {
    val pattern = Pattern.compile("-?\\d+")
    if (strNum == null) return false
    pattern.matcher(strNum).matches
  }

  def isPhoneNumber(strNum: String): Boolean = {
    val pattern = Pattern.compile("^\\d{3}-\\d{3}-\\d{4}$")
    if (strNum == null) return false
    pattern.matcher(strNum).matches
  }

  def getSubMenuText(itemList: java.util.List[Item], group: String): Array[String] = {
    val maxItemsPerReply = 15
    val itemSeq = itemList.asScala.filter(item=>group.equals(item.group()))
    // ordering for pretty printing
    implicit val orderingForItem: Ordering[Item] = (x: Item, y: Item) =>
      x.name().trim.length.compareTo(y.name().trim.length)
    val reOrderedList = itemSeq.sorted
    val grouped = reOrderedList.grouped(maxItemsPerReply)
    val res = grouped.toList.filter(it => it.nonEmpty).map(it => {
      it.map(item =>
        s"""
           |${item.name.trim}
           |${item.price} ??????
           |?????????<b>/${item.id}</b>???
           |""".stripMargin).reduce((v1, v2) => s"$v1$v2")
    })
    res.toArray
  }

  def getEmailOrderText(order: Order): String =
    s"""
       |<H3 style="color: green">???????? ??????????????????????:</H3>
       |<b>????????:</b> ${order.date}
       |
       |<br/><b>ID ??????????????????????:</b> ${order.userId}
       |
       |<br/><b>ID ????????????????????:</b> ${order.orderId}
       |
       |<br/><b>????'??:</b> ${order.userName}
       |
       |<br/><b>?????????? ????????????????:</b> ${if (order.userPhoneNumber!=null) order.userPhoneNumber else "[?????????? ???? ????????????????/???????????????? ???? ??????????]"}
       |
       |<H3 style="color: green">????????????????????:</H3>
       |   ${order.orderBucket.map(it=>s"""${it.toString}""").reduce((s1,s2)=>s"$s1<br/>$s2")}
       |
       |""".stripMargin


  def getNameEditingText(userId: Long): String =
    s"""|????????
        |?????????? ?????????????????????? $userId/??????
        |
        |???????? ??????????, ??????????????????. ?????????? ?????????????????????????? ??????????????:
        |???????????????? ????'??.
        |??????????, ???? ???? ???????????? ???????????????????? ???????????????????? ???????????????? ??????????.
        |""".stripMargin


  def getPhoneEditingText(userId: Long): String = {
    s"""|????????
        |?????????? ?????????????????????? $userId/??????????????
        |
        |??????????????, ???????? ??????????, ?????????? ????????????????
        |?? ???????????????????? ??????????????:
        |     xxx-xxx-xxxx
        |""".stripMargin
  }
  def getPersonalDataEditingSectionText(botVisitor: BotVisitor): String = {

    val phoneNumber = if (botVisitor.getPhoneNumber==null) "" else botVisitor.getPhoneNumber

    s"""|????ID ??????????????????????:
        |${botVisitor.getUserId}
        |
        |????????????'??/????????????????:
        |${if (botVisitor.getName!=null) botVisitor.getName else botVisitor.getUser.getFirstName}
        |
        |??????????????????:
        |$phoneNumber
        |""".stripMargin
  }

  def getMainMenuText(botVisitor: BotVisitor): String = {
    getMainMenuText(BotVisitorToScalaBotVisitorConverter.convertBotVisitorToScalaBotVisitor(botVisitor))
  }
  
  def getMainMenuText(botVisitor: ScalaBotVisitor): String ={
    val name = if (botVisitor.name!=null) botVisitor.name else botVisitor.user.getFirstName
    s"""|????????????, $name!
        |
        |???? ?????? ?????? ???????????????????? ??????????????, ???????????????? ???????????? ?????? ?????????????????? ???? ????????.
        |?????? ?????????????????????? ?????????????????? ???? ????????????????, ???? ???????????????????? ????????????, ????????????????????
        |???? ID ??????????.
        |
        |?????????????????????? ???????????????????? ???????????????????????? ?? ??????????????.""".stripMargin}

  def getPhoneNumberReceivedText(): String =
    s"""|????
        |??????????????. ?????????? ?????????????????? ?? ?????????????????? ??????????????.""".stripMargin


  def getFullNameReceivedText(): String =
    s"""|????
        |??????????????. ????'?? ?????????????????? ?? ?????????????????? ??????????????.""".stripMargin

  def getItemQtyWrongEnteredNumber(botVisitor: ScalaBotVisitor): String =
    s"""|?????????????????
        |?????????????? ???????????????????? ?????????? ?? ???????????? ?????????????????????? ??????????????????.
        |?????????????? ?????????????????? ???????????? <b>??????????</b> 0??????1??????2??????3?????? ???????????????????? ????????????.
        |
        |${botVisitor.itemToEditQty.toString3}
        |""".stripMargin

  def getItemNotAcceptedText(): String =
    s"""
       |?????????????
       |???? ?????? ?????????? ???????????????????? ID,
       |?????? ???????????????????? ?????????? ???????? ???????????? ID ????????????
       |?????? ?????????????? ???????? ????????, ???????? ???? ???? ??????????????????????.
       |??????????????????, ????????-?????????? ???? ??????????.
       |""".stripMargin

  def getContactsMsg(): String =
    s"""
       |??????????????: ???????????? ?????????? ??????????????????????????
       |
       |??????: 097-257-0077
       |""".stripMargin

}
