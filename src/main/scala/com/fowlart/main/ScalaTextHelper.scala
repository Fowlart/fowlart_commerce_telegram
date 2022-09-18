package com.fowlart.main


class ScalaTextHelper {

  def getMainMenuText(name: String): String ={
    s"""Привіт, $name 🤝
       |Це бот для замовлення товарів, натискай кнопки для навігації по меню ⚡️""".stripMargin
  }

}
