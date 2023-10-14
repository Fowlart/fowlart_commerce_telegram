package com.fowlart.main

import com.fowlart.main.state.cosmos.Item


class ScalaHelper {

  def getBucketContent(cart: java.util.List[String]): String = {

    val items = cart.toArray.toList.asInstanceOf[List[String]]

    items.map(i =>
      s"""
         |<p class="items">$i<input class="qty-input" value="10" placeholder="кількість від 1 шт" type="number" min="1" onchange="sendItems()"><button class="remove-item-button" onclick="deleteElement('$i')">xxx</button></p>
         |""".stripMargin)
      .mkString("\n")
  }
  def getButtonHtml(i: Item): String =
    s"""<button class="w3-button w3-block w3-black" onclick="changeImage('pdp/img/${i.id()}','${i.name()} 💲${i.price()}грн');">${i.name()}</button>"""

   def getGroupLinkHtml(name: String): String =
        s"""<a href="?group=$name" class="w3-bar-item w3-button">${name.toLowerCase}</a>"""

}
