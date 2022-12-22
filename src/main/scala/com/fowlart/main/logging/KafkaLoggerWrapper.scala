package com.fowlart.main.logging

import com.google.gson.Gson
import org.apache.logging.log4j.core.Logger

class KafkaLoggerWrapper(logger: Logger) {

  def info(msgObj: Any): Unit = {
    val gson = new Gson()
    val json = gson.toJson(msgObj)
    logger.info(json)
  }

  def debug(msgObj: Any): Unit = {
    val gson = new Gson()
    val json = gson.toJson(msgObj)
    logger.debug(json)
  }

}
