package com.fowlart.main.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Set;

@JsonSerialize
public record BotVisitorDto(String id, String firstName, Set<String> bucket, String phone,
                            String telegramFirstName, String telegramLastName) {
}
