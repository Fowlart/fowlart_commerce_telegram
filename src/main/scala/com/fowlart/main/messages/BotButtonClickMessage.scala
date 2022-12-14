package com.fowlart.main.messages
import com.fowlart.main.ScalaBotVisitor

trait BotButtonClickMessage

case class SimpleTextBotButtonClickMessage(buttonClicked: String, scalaBotVisitor: ScalaBotVisitor) 
  extends BotButtonClickMessage
  


