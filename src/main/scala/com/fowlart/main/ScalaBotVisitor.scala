package com.fowlart.main

import com.fowlart.main.in_mem_catalog.Item
import org.telegram.telegrambots.meta.api.objects.User

case class ScalaBotVisitor(
                            name: String,
                            isNameEditingMode: Boolean,
                        phoneNumber: String, 
                        isPhoneNumberFillingMode: Boolean,
                        itemToEditQty: Item,
                        user: User, 
                        userId: String, 
                        bucket: Set[Item])
