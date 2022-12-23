package com.fowlart.main.logging

import com.fowlart.main.BotVisitorToScalaBotVisitorConverter
import com.fowlart.main.state.{BotVisitor, ScalaBotVisitor}
import com.google.gson.Gson
import org.apache.logging.log4j.core.Logger

import java.util.Date

object LoggerHelper {

  val fileLogger: Logger = LoggerBuilder.getFileLogger

  val kafkaLogger: Logger = LoggerBuilder.getKafkaLogger

  def logInfoInFile(msg: String): Unit = fileLogger.info(msg)
  def logWarningInFile(msg: String) = fileLogger.warn(msg)

  def logErrorInFile(msg: String) = fileLogger.error(msg)

  def logErrorInFile(msg: String,throwable: Throwable) = fileLogger.error(msg,throwable)

  def logDebugInFile(msg: String) = fileLogger.debug(msg)
  def logFatalInFile(msg: String) = fileLogger.fatal(msg)

  // kafka
  def logSimpleInfoMsgInKafka(msg: String) = kafkaLogger.info(msg)

  def logSimpleErrorMsgInKafka(msg: String) = kafkaLogger.error(msg)

}
