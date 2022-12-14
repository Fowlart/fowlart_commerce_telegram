package com.fowlart.main.messages

import com.fowlart.main.ScalaBotVisitor
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

trait HandlerResponse

case class ResponseWithSendMessageAndScalaBotVisitor(sendMessageResponse: SendMessage,
                                                     scalaBotVisitor: ScalaBotVisitor) extends HandlerResponse

