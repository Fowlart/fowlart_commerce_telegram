package com.fowlart.main.logging

import com.fowlart.main.state.ScalaBotVisitor

case class LoggerMessage(date: String, scalaBotVisitor: ScalaBotVisitor, msg: String)
