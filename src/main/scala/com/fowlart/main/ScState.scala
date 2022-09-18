package com.fowlart.main

enum ScState {
  case MAIN_SCREEN, CATALOG, DEBT, DELIVERY

  // could contains methods
  def mainScreen: ScState = MAIN_SCREEN

  def delivery: ScState = DELIVERY

  def catalog: ScState = CATALOG

  def debt: ScState = DEBT
}

enum ScStateWithBit(bit: Int) {
  case MAIN_SCREEN extends ScStateWithBit(10)
  case CATALOG extends ScStateWithBit(20)
  case DEBT extends ScStateWithBit(30)
  case DELIVERY extends ScStateWithBit(40)

  def getBit: Int = bit

}

object ScStateEx extends App {
  val stateWithBit = ScStateWithBit.DEBT
  print(stateWithBit.getBit)
}
