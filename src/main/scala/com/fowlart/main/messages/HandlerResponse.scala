package com.fowlart.main.messages

import org.telegram.telegrambots.meta.api.methods.send.SendMessage

trait HandlerResponse

case class ResponseWithSendMessage(sendMessageResponse: SendMessage) extends HandlerResponse

