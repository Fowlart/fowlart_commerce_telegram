package com.fowlart.main.logging

import org.apache.logging.log4j.core.Logger

object LoggerHelper {

  private val logger: Logger = LoggerBuilder.getLogger

  def logInfoInFile(msg: String) = logger.info(msg)
  def logWarningInFile(msg: String) = logger.warn(msg)

  def logErrorInFile(msg: String) = logger.error(msg)

  def logErrorInFile(msg: String, throwable: Throwable) =
    logger.error(msg, throwable)

  def logDebugInFile(msg: String) = logger.debug(msg)
  def logFatalInFile(msg: String) = logger.fatal(msg)

}
