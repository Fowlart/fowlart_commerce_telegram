package com.fowlart.main

import com.fowlart.main.state.cosmos.Item


class ScalaHelper {

  def getButtonHtml(i: Item): String =
    s"""<button class="w3-button w3-block w3-black" onclick="changeImage('/pdp/img/${i.id()}','${i.name()} 💲${i.price()}грн');">${i.name()}</button>"""

   def getGroupLinkHtml(name: String): String =
        s"""<a href="/pdp/public_catalog?group=$name" class="w3-bar-item w3-button">$name</a>"""

}
