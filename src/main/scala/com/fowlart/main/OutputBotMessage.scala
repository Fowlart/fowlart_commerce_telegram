package com.fowlart.main

import com.fowlart.main.state.BotVisitor
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

case class OutputBotMessage (botVisitor: BotVisitor,sendMessage: SendMessage)
