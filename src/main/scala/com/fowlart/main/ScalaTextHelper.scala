package com.fowlart.main

class ScalaTextHelper {

  def getMainMenuText(name: String): String ={
    s"""Привіт, $name!
       |Оберай з наступного списку:
       |1/ каталог
       |2/ доставка
       |3/ борги""".stripMargin
  }

}
