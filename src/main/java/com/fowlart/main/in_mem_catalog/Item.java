package com.fowlart.main.in_mem_catalog;

import java.io.Serializable;

public record Item (String id, String name, Double price, String group) implements Serializable {}
