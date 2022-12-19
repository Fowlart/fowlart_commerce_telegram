package com.fowlart.main.messages

import com.fowlart.main.state.ScalaBotVisitor
import org.telegram.telegrambots.meta.api.methods.send.{SendMessage, SendPhoto}

trait HandlerResponse {
  def scalaBotVisitor(): ScalaBotVisitor
}

case class ResponseWithSendMessageAndScalaBotVisitor(sendMessageResponse: SendMessage,
                                                     scalaBotVisitor: ScalaBotVisitor) extends HandlerResponse

case class ResponseWithPhotoMessageAndScalaBotVisitor(photoMessage: SendPhoto,
                                                      scalaBotVisitor: ScalaBotVisitor) extends HandlerResponse

