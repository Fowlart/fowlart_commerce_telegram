package com.fowlart.main.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record KeyDto(String name, String key) {
}
