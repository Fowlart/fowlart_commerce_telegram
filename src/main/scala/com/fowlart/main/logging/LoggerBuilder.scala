package com.fowlart.main.logging

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.{Filter, Logger}
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory

object LoggerBuilder {

  private lazy val loggerContext = {

    val builder = ConfigurationBuilderFactory.newConfigurationBuilder
    builder.setStatusLevel(Level.ALL)
    builder.setConfigurationName("BOTLog4JConfigs")
    builder.add(
      builder
        .newFilter(
          "ThresholdFilter",
          Filter.Result.ACCEPT,
          Filter.Result.NEUTRAL
        )
        .addAttribute("level", Level.INFO)
    )

    /** <h3>CONSOLE appender <h3> */
    val consoleAppenderBuilder = builder
      .newAppender("Stdout", "CONSOLE")
      .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
    consoleAppenderBuilder.add(
      builder
        .newLayout("PatternLayout")
        .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable")
    )
    consoleAppenderBuilder.add(
      builder
        .newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
        .addAttribute("marker", "FLOW")
    )
    builder.add(consoleAppenderBuilder)

    /** <h3>FILE appender <h3> */
    val filePattern = "logs/%d{MM-dd-yyyy}.log.gz"

    val fileAppenderBuilder = builder
      .newAppender("FileAppender", "RollingFile")
      fileAppenderBuilder.addAttribute("filePattern",filePattern)
    fileAppenderBuilder.addAttribute("ignoreExceptions","false")

    fileAppenderBuilder.addComponent(builder.newComponent("TimeBasedTriggeringPolicy"))

    fileAppenderBuilder.add(
      builder
        .newLayout("PatternLayout")
        .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable")
    )
    builder.add(fileAppenderBuilder)

    //configure Root logger as FileLogger
    builder.add(builder
        .newRootLogger(Level.INFO)
        //switch loggers HERE
        .add(builder.newAppenderRef("FileAppender")))

    Configurator.initialize(builder.build)
  }

  def getLogger: Logger = loggerContext.getRootLogger
}
