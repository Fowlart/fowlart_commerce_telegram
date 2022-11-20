package com.fowlart.main.messages

import com.fowlart.main.KeyboardHelper
import com.fowlart.main.in_mem_catalog.Catalog
import com.fowlart.main.state.{BotVisitor, BotVisitorService}
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

trait BotMessage {

}

case class RemoveItemFromBucketMessage(visitor: BotVisitor,
                                       botVisitorService: BotVisitorService,
                                       keyboardHelper: KeyboardHelper) extends BotMessage

case class EditQtyForItemMessage(qty: Int,
                                 botVisitor: BotVisitor,
                                 botVisitorService: BotVisitorService,
                                 keyboardHelper: KeyboardHelper) extends BotMessage

case class ItemAddToBucketMessage(item: String,
                                  visitor: BotVisitor,
                                  botVisitorService: BotVisitorService,
                                  catalog: Catalog,
                                  sendMessage: SendMessage) extends BotMessage
