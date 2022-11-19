package com.fowlart.main

trait Msg

case class InputBotUpdateWithText(textOrNumber: Any) extends Msg
