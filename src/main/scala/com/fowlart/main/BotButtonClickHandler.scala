package com.fowlart.main
import com.fowlart.main.messages._

object BotButtonClickHandler {
  
  def handleButtonClick(botButtonClickMessage: BotButtonClickMessage): Unit = {
    
     botButtonClickMessage match {
       
       case SimpleTextBotButtonClickMessage("buttonClicked", visitor) => {
         
       }
     
     }
    
  }

}
