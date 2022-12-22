package com.fowlart.main.logging

import com.google.gson.Gson
import org.apache.logging.log4j.core.Logger

class LoggerWrapper(val logger: Logger) {

  val gson = new Gson()

  def info(msgObj: Any): Unit = {
    val m = gson.toJson(msgObj)
    println(m)
    logger.info(m)
  }

  def debug(msgObj: Any): Unit = {
    val m = gson.toJson(msgObj)
    println(m)
    logger.debug(m)
  }

  def warn(msgObj: Any): Unit = {
    val m = gson.toJson(msgObj)
    println(m)
    logger.warn(m)
  }

  def error(msgObj: Any): Unit = {
    val m = gson.toJson(msgObj)
    println(m)
    logger.error(m)
  }

  def fatal(msgObj: Any): Unit = {
    val m = gson.toJson(msgObj)
    println(m)
    logger.fatal(m)
  }
}
