package com.fowlart.main.logging

import com.fowlart.main.BotVisitorToScalaBotVisitorConverter
import com.fowlart.main.state.{BotVisitor, ScalaBotVisitor}
import com.google.gson.Gson

import java.util.Date

object LoggerHelper {

  val fileLogger: LoggerWrapper = LoggerBuilder.getFileLogger

  val kafkaLogger: LoggerWrapper = LoggerBuilder.getKafkaLogger

  def logInfoInFile(botVisitor: BotVisitor, msg: String): Unit = logInfoInFile(
    BotVisitorToScalaBotVisitorConverter.convertBotVisitorToScalaBotVisitor(
      botVisitor
    ),
    msg
  )

  def logInfoInFile(scalaBotVisitor: ScalaBotVisitor, msg: String): Unit = {
    val date = new Date()
    fileLogger.info( LoggerMessage(date.toString, scalaBotVisitor, msg))
  }

  def logWarningInFile(scalaBotVisitor: ScalaBotVisitor, msg: String) = {
    val date = new Date()
    fileLogger.warn( LoggerMessage(date.toString, scalaBotVisitor, msg))
  }

  def logErrorInFile(scalaBotVisitor: ScalaBotVisitor, msg: String) = {
    val date = new Date()
    fileLogger.error(LoggerMessage(date.toString, scalaBotVisitor, msg))
  }

  def logDebugInFile(scalaBotVisitor: ScalaBotVisitor, msg: String) = {
    val date = new Date()
    fileLogger.debug(LoggerMessage(date.toString, scalaBotVisitor, msg))
  }

  def logFatalInFile(scalaBotVisitor: ScalaBotVisitor, msg: String) = {
    val date = new Date()
    fileLogger.fatal(LoggerMessage(date.toString, scalaBotVisitor, msg))
  }

  def logSimpleInfoMsgInFile(msg: String) = {
    val date = new Date()
    fileLogger.info(LoggerSimpleMessage(date.toString, msg))
  }

  def logSimpleErrorMsgInFile(msg: String) = {
    val date = new Date()
    fileLogger.error(LoggerSimpleMessage(date.toString, msg))
  }

  def logSimpleInfoMsgInKafka(msg: String) = {
    val date = new Date()
    kafkaLogger.info(LoggerSimpleMessage(date.toString, msg))
  }

  def logSimpleErrorMsgInKafka(msg: String) = {
    val date = new Date()
    kafkaLogger.error(LoggerSimpleMessage(date.toString, msg))
  }


}
