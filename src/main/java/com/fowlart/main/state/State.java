package com.fowlart.main.state;

import java.io.Serializable;

public enum State implements Serializable {

    MAIN_SCREEN("1_4"), CATALOG("1_1"), DEBT("1_3"), DELIVERY("1_2");

    public final String textCode;

    State(String textCode){
        this.textCode = textCode;
    }

    public String getTextCode() {
        return textCode;
    }
}
