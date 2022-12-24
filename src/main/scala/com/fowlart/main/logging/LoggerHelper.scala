package com.fowlart.main.logging

import com.fowlart.main.BotVisitorToScalaBotVisitorConverter
import com.fowlart.main.state.{BotVisitor, ScalaBotVisitor}
import com.google.gson.Gson
import org.apache.logging.log4j.core.Logger

import java.util.Date

object LoggerHelper {

  private val fileLogger: Logger = LoggerBuilder.getFileLogger

  def logInfoInFile(msg: String) = fileLogger.info(msg)
  def logWarningInFile(msg: String) = fileLogger.warn(msg)

  def logErrorInFile(msg: String) = fileLogger.error(msg)

  def logErrorInFile(msg: String,throwable: Throwable) = fileLogger.error(msg,throwable)

  def logDebugInFile(msg: String) = fileLogger.debug(msg)
  def logFatalInFile(msg: String) = fileLogger.fatal(msg)

}
