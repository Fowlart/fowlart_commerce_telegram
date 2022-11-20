package com.fowlart.main

import com.fowlart.main.in_mem_catalog.Catalog
import com.fowlart.main.state.{BotVisitor, BotVisitorService}
import org.telegram.telegrambots.meta.api.objects.Update

trait Msg


case class InputBotUpdate(update: Update,
                          service: BotVisitorService,
                          keyboardHelper: KeyboardHelper,
                          catalog: Catalog) extends Msg
