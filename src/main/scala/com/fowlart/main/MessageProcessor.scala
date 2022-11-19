package com.fowlart.main

object MessageProcessor {

  def processMessage(msg: Msg) = {
    msg match {
      case InputBotUpdateWithText(callbackStringData) => {
        callbackStringData match {
          case msg: String if msg.startsWith(REMOVE_COMMAND)  => {
            println("Removing item case")

          }
          case msg: String if msg.startsWith(GOOD_ADD_COMMAND) => {
            println("Adding item case")
          }
          case a: Int => println("Qty provided")

          case _ => println("Unpredictable result")
        }
      }
    }
  }
}
