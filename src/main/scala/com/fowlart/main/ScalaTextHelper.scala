package com.fowlart.main

import com.fowlart.main.in_mem_catalog.Item

class ScalaTextHelper {

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
         |Товар додано:
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
       |Або натисніть /remove якщо хочете змінусувати товар з корзини.
       |""".stripMargin

  def getContactsMsg(): String =
    s"""
       |Андрій Скіра Володимирович,
       |тел: 097-257-0077
       |""".stripMargin

}
