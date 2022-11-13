package com.fowlart.main

import com.fowlart.main.in_mem_catalog.Item
import scala.collection.JavaConverters._

class ScalaTextHelper {

  def getSubMenuText(itemList: java.util.List[Item], group: String): Array[String] = {
    val maxItemsPerReply = 15;
    val itemSeq = itemList.asScala.filter(item=>group.equals(item.group()))
    // ordering for pretty printing
    implicit val orderingForItem: Ordering[Item] = (x: Item, y: Item) =>
      x.name().trim.length.compareTo(y.name().trim.length)
    val reOrderedList = itemSeq.sorted
    val grouped = reOrderedList.grouped(maxItemsPerReply)
    val res = grouped.toList.filter(it => it.nonEmpty).map(it => {
      it.map(item =>
        s"""
           | ⏺ ${item.name.trim.toLowerCase}
           |${item.price} грн
           |    /${item.id}
           |""".stripMargin).reduce((v1, v2) => s"$v1$v2")
    })
    res.toArray
  }
  def getMainMenuText(name: String): String ={
    s"""|Привіт!
        |
        |Це бот для замовлення товарів, натискай кнопки для навігації по меню.
        |Або продовжуйте навігацію по каталогу, чи замовляйте товари, натискаючи
        |їх ID номер.
        |
        |Редагування кількостей відбувається в корзині.""".stripMargin}

    def getItemAcceptedText(item: Item): String = {
      s"""
         |Додано:
         |
         |${item.toString}
         |
         |Подальше редагування кількості відбувається у корзині.
         |""".stripMargin
    }

  def getItemNotAcceptedText(): String = {
    s"""
       |Ви або ввели некоректне ID,
       |або додатковий текст крім самого ID товару
       |або зробили щось таке, чого ми не передбачили.
       |Спробуйте, будь-ласка ще разок.
       |""".stripMargin
  }

  def getEditItemQtyMsg(item: Item): String =
    s"""
       |Редагуємо товар:
       |
       |$item
       |
       |Введіть кількість товару цілим позитивним числом.
       |
       |Або натисніть /remove - видалити.
       |""".stripMargin

  def getContactsMsg(): String =
    s"""
       |Власник: Андрій Скіра Володимирович
       |тел: 097-257-0077
       |""".stripMargin

}
